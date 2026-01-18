package me.serbob.antipiracy

import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectionPlugin : Plugin<Project> {

    override fun apply(
        project: Project
    ) {
        val extension = project.extensions.create(
            "antipiracy",
            InjectionExtension::class.java,
            project
        )

        project.tasks.register("injectLicenseFields", InjectionTask::class.java) { task ->
            task.jarFile.set(extension.jarFile)
            task.fields.set(extension.fields)
            task.includeDefaults.set(extension.includeDefaults)
            task.hiddenNonce.set(extension.hiddenNonce)
        }
    }
}