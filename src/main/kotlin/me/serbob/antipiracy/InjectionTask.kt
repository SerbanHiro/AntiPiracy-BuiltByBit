package me.serbob.antipiracy

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.*
import java.io.File
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

    @TaskAction
    fun inject() {
        val jar = jarFile.get().asFile
        val tempJar = jar.resolveSibling("${jar.nameWithoutExtension}-temp.jar")

        ZipFile(jar).use { zip ->
            ZipOutputStream(tempJar.outputStream()).use { zos ->
                zip.entries().asSequence().forEach { entry ->
                    val content = zip.getInputStream(entry).readBytes()

                    val output = if (entry.name.endsWith(".class") && "module-info" !in entry.name) {
                        injectFields(content)
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
    }

    private fun injectFields(
        classBytes: ByteArray
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

                val allFields = if (includeDefaults.get()) {
                    InjectionConstants.DEFAULT_FIELDS + fields.get()
                } else {
                    fields.get()
                }

                allFields.forEach { (name, value) ->
                    super.visitField(accessFlags, name, "Ljava/lang/String;", null, value)?.visitEnd()
                }
                super.visitEnd()
            }
        }

        reader.accept(visitor, 0)
        return writer.toByteArray()
    }
}