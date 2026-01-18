plugins {
    kotlin("jvm") version "2.2.0"

    id("com.gradle.plugin-publish") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.asm)
}

gradlePlugin {
    website.set("https://github.com/SerbanHiro/AntiPiracy-BuiltByBit")
    vcsUrl.set("https://github.com/SerbanHiro/AntiPiracy-BuiltByBit")

    plugins {
        create("antipiracy") {
            id = "me.serbob.antipiracy"
            displayName = "Anti-Piracy Placeholders BuiltByBit"
            description = "Injects BuiltByBit's antipiracy placeholders into all classes"
            tags.set(listOf("minecraft", "builtbybit", "license", "anti-piracy"))
            implementationClass = "me.serbob.antipiracy.InjectionPlugin"
        }
    }
}