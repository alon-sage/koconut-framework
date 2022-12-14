import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

repositories {
    mavenCentral()
}

with(project.dependencies) {
    configurations.all {
        withDependencies {
            add(platform(project(":koconut-dependencies")))
        }
        when (name) {
            "kapt" -> withDependencies {
                add(create("com.google.auto.service:auto-service"))
            }

            "testImplementation" -> withDependencies {
                add(create(kotlin("test")))
                add(create("io.mockk:mockk"))
                add(create("org.spekframework.spek2:spek-dsl-jvm"))
            }

            "testRuntimeOnly" -> withDependencies {
                add(create("org.spekframework.spek2:spek-runner-junit5"))
            }
        }
    }
}

tasks.withType<KotlinCompile<*>> {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<KotlinCompile<KotlinJvmOptions>> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform { includeEngines("spek2") }
    reports {
        junitXml.required.set(true)
    }
}