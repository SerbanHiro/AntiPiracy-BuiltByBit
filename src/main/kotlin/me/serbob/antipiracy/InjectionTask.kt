package me.serbob.antipiracy

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
    abstract val fields: ListProperty<Pair<String, String>>

    @get:Input
    abstract val includeDefaults: Property<Boolean>

    @get:Input
    abstract val hiddenNonce: Property<Boolean>

    @get:Input
    var realNonceClass: String = "nothing was chosen, report this on the github issues page"

    @TaskAction
    fun inject() {
        val jar = jarFile.get().asFile
        val tempJar = jar.resolveSibling("${jar.nameWithoutExtension}-temp.jar")

        ZipFile(jar).use { zip ->
            ZipOutputStream(tempJar.outputStream()).use { zos ->
                val entries = zip.entries().asSequence().toList()

                val classEntries = entries.filter {
                    it.name.endsWith(".class") && "module-info" !in it.name
                }
                val realNonceEntry = classEntries.randomOrNull()
                realNonceClass = realNonceEntry?.name?.removeSuffix(".class") ?: ""

                entries.forEach { entry ->
                    val content = zip.getInputStream(entry).readBytes()
                    val isRealNonceClass = entry.name.removeSuffix(".class") == realNonceClass

                    val output = if (entry.name.endsWith(".class") && "module-info" !in entry.name) {
                        injectFields(content, isRealNonceClass)
                    } else {
                        content
                    }

                    zos.putNextEntry(ZipEntry(entry.name))
                    zos.write(output)
                    zos.closeEntry()
                }
            }
        }

        Files.move(tempJar.toPath(), jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
        logger.lifecycle("Injected license fields into all classes for ${jar.name}")

        if (!hiddenNonce.get())
            return

        saveNonceMapping(jar.nameWithoutExtension, realNonceClass)
    }

    private fun saveNonceMapping(
        version: String,
        nonceClass: String
    ) {
        val nonceFile = project.rootDir.resolve("nonce-mappings.properties")

        val properties = java.util.Properties()
        if (nonceFile.exists()) {
            nonceFile.inputStream().use { properties.load(it) }
        }

        properties[version] = nonceClass

        nonceFile.outputStream().use {
            properties.store(it, "Real nonce class mappings - DO NOT SHARE")
        }

        logger.lifecycle("Saved nonce mapping: $version -> $nonceClass")
    }

    private fun injectFields(
        classBytes: ByteArray,
        isRealNonceClass: Boolean
    ): ByteArray {
        val reader = ClassReader(classBytes)
        val writer = ClassWriter(reader, 0)

        val visitor = object : ClassVisitor(Opcodes.ASM9, writer) {
            private var isInterface = false
            private var className: String? = null

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

                val allFields = if (includeDefaults.get()) {
                    InjectionConstants.generateRandomFields() + fields.get()
                } else {
                    fields.get()
                }

                allFields.forEach { (name, value) ->
                    val shouldHideNonce = hiddenNonce.get()
                            && value == InjectionConstants.noncePlaceholder()
                            && !isRealNonceClass

                    val fieldValue = if (shouldHideNonce) {
                        RandomUtil.randomFakeNonce()
                    } else {
                        value
                    }

                    super.visitField(accessFlags, name, "Ljava/lang/String;", null, fieldValue)?.visitEnd()
                }
                super.visitEnd()
            }
        }

        reader.accept(visitor, 0)
        return writer.toByteArray()
    }
}