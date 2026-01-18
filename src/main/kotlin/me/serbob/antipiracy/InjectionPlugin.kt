package me.serbob.antipiracy

import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectionPlugin : Plugin<Project> {

    override fun apply(
        project: Project
    ) {
        project.tasks.register("injectLicenseFields", InjectionTask::class.java)
    }
}