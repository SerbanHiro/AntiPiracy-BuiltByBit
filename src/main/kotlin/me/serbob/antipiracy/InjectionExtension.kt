package me.serbob.antipiracy

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

abstract class InjectionExtension(
    private val project: Project
) {

    abstract val jarFile: RegularFileProperty
    abstract val fields: ListProperty<Pair<String, String>>
    abstract val includeDefaults: Property<Boolean>

    init {
        fields.convention(listOf())
        includeDefaults.convention(true)
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
        fields.add(name to value)
    }
}