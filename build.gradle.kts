import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20" apply false
    kotlin("kapt") version "1.7.20" apply false
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

group = "dev.koconut"
version = "1.0.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    afterEvaluate {
        if ("implementation" in configurations.names) dependencies {
            add("implementation", enforcedPlatform(project(":dependencies-bom")))
        }

        if ("kapt" in configurations.names) dependencies {
            add("kapt", enforcedPlatform(project(":dependencies-bom")))
        }

        if ("protobuf" in configurations.names) dependencies {
            add("protobuf", enforcedPlatform(project(":dependencies-bom")))
        }

        if ("kaptTest" in configurations.names) dependencies {
            add("kaptTest", enforcedPlatform(project(":dependencies-bom")))
        }

        if ("javaAgent" in configurations.names) dependencies {
            add("javaAgent", enforcedPlatform(project(":dependencies-bom")))
        }

        if ("testImplementation" in configurations.names) dependencies {
            add("testImplementation", kotlin("test"))
            add("testImplementation", "io.mockk:mockk")
            add("testImplementation", "org.spekframework.spek2:spek-dsl-jvm")
            add("testRuntimeOnly", "org.spekframework.spek2:spek-runner-junit5")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            allWarningsAsErrors = true
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform { includeEngines("spek2") }
        reports {
            junitXml.required.set(true)
        }
    }
}