package me.serbob.antipiracy

import me.serbob.antipiracy.model.Field
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

abstract class InjectionExtension(
    private val project: Project
) {

    abstract val jarFile: RegularFileProperty
    abstract val fields: ListProperty<Field>
    abstract val includeDefaults: Property<Boolean>
    abstract val hiddenNonce: Property<Boolean>

    init {
        fields.convention(listOf())
        includeDefaults.convention(true)
        hiddenNonce.convention(true)
        jarFile.convention(
            project.layout.buildDirectory.file("libs/${project.name}-${project.version}.jar")
        )
    }

    fun target(
        path: String
    ) {
        jarFile.set(project.file(path))
    }

    fun target(
        file: File
    ) {
        jarFile.set(file)
    }

    fun targetBuildLibs(
        path: String
    ) {
        jarFile.set(project.file("build/libs/$path"))
    }

    fun noDefaultFields() {
        includeDefaults.set(false)
    }

    fun addField(
        name: String,
        value: String
    ) {
        fields.add(Field(name, value))
    }

    fun addRandomField(
        value: String
    ) {
        fields.add(Field("", value))
    }

    fun addNonceField(
        name: String,
        value: String
    ) {
        fields.add(Field(name, value, true))
    }

    fun addRandomNonceField(
        value: String
    ) {
        fields.add(Field("", value, true))
    }

    fun disableHiddenNonce() {
        hiddenNonce.set(false)
    }
}