package me.serbob.antipiracy

import me.serbob.antipiracy.model.Field
import me.serbob.antipiracy.util.RandomUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

abstract class InjectionTask : DefaultTask() {

    init {
        group = "build"
        description = "Injects BuiltByBit's antipiracy placeholders into all classes"
    }

    @get:InputFile
    abstract val jarFile: RegularFileProperty

    @get:Input
    abstract val fields: ListProperty<Field>

    @get:Input
    abstract val includeDefaults: Property<Boolean>

    @get:Input
    abstract val hiddenNonce: Property<Boolean>

    @TaskAction
    fun inject() {
        val jar = jarFile.get().asFile
        val tempJar = jar.resolveSibling("${jar.nameWithoutExtension}-temp.jar")

        val fieldTemplates = buildFieldTemplates()

        ZipFile(jar).use { zip ->
            ZipOutputStream(tempJar.outputStream()).use { zos ->
                val entries = zip.entries().asSequence().toList()

                val classEntries = entries.filter {
                    it.name.endsWith(".class") && "module-info" !in it.name
                }

                val nonceTemplates = fieldTemplates.filter { it.nonce }
                val shuffledClasses = classEntries.shuffled()

                val nonceClassMappings: Map<Field, String> = nonceTemplates
                    .mapIndexed { index, field ->
                        val className = shuffledClasses.getOrNull(index)
                            ?.name
                            ?.removeSuffix(".class")
                            ?: ""

                        field to className
                    }.toMap()

                entries.forEach { entry ->
                    val content = zip.getInputStream(entry).readBytes()
                    val className = entry.name.removeSuffix(".class")

                    val output = if (entry.name.endsWith(".class") && "module-info" !in entry.name) {
                        injectFields(content, className, nonceClassMappings, fieldTemplates)
                    } else {
                        content
                    }

                    zos.putNextEntry(ZipEntry(entry.name))
                    zos.write(output)
                    zos.closeEntry()
                }

                if (hiddenNonce.get()) {
                    saveNonceMapping(jar.nameWithoutExtension, nonceClassMappings)
                }
            }
        }

        Files.move(tempJar.toPath(), jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
        logger.lifecycle("Injected license fields into all classes for ${jar.name}")
    }

    private fun buildFieldTemplates(): List<Field> {
        val defaults = if (includeDefaults.get()) {
            InjectionConstants.generateFieldTemplates() + InjectionConstants.generateDefaultNonceTemplate()
        } else {
            emptyList()
        }

        return defaults + fields.get()
    }

    private fun saveNonceMapping(
        version: String,
        nonceClassMappings: Map<Field, String>
    ) {
        val nonceFile = project.rootDir.resolve("nonce-mappings.properties")

        val properties = java.util.Properties()
        if (nonceFile.exists()) {
            nonceFile.inputStream().use { properties.load(it) }
        }

        nonceClassMappings.forEach { (field, className) ->
            properties["$version.${field.value}"] = className
        }

        nonceFile.outputStream().use {
            properties.store(it, "Real nonce class mappings - DO NOT SHARE")
        }

        logger.lifecycle("Saved ${nonceClassMappings.size} nonce mapping(s) for $version")
    }

    private fun injectFields(
        classBytes: ByteArray,
        className: String,
        nonceClassMappings: Map<Field, String>,
        fieldTemplates: List<Field>
    ): ByteArray {
        val reader = ClassReader(classBytes)
        val writer = ClassWriter(reader, 0)

        val visitor = object : ClassVisitor(Opcodes.ASM9, writer) {
            private var isInterface = false

            override fun visit(
                version: Int,
                access: Int,
                name: String?,
                signature: String?,
                superName: String?,
                interfaces: Array<out String>?
            ) {
                isInterface = (access and Opcodes.ACC_INTERFACE) != 0
                super.visit(version, access, name, signature, superName, interfaces)
            }

            override fun visitEnd() {
                val accessFlags = if (isInterface) {
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL
                } else {
                    Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL
                }

                val nonNonceTemplates = fieldTemplates.filter { !it.nonce }

                nonNonceTemplates.forEach { template ->
                    val fieldName = template.name.ifEmpty {
                        RandomUtil.randomValidJavaString()
                    }

                    super.visitField(accessFlags, fieldName, "Ljava/lang/String;", null, template.value)
                        ?.visitEnd()
                }

                val assignedNonceEntry = nonceClassMappings.entries.find { it.value == className }

                val fieldName = if (assignedNonceEntry != null && assignedNonceEntry.key.name.isNotEmpty()) {
                    assignedNonceEntry.key.name
                } else {
                    RandomUtil.randomValidJavaString()
                }

                val fieldValue = assignedNonceEntry?.key?.value ?: RandomUtil.randomFakeNonce()

                super.visitField(accessFlags, fieldName, "Ljava/lang/String;", null, fieldValue)
                    ?.visitEnd()

                super.visitEnd()
            }
        }

        reader.accept(visitor, 0)
        return writer.toByteArray()
    }
}