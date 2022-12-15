import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

repositories {
    mavenCentral()
}

with(dependencies) {
    configurations.all {
        withDependencies {
            add(platform(project(":koconut-dependencies")))
        }
        when (name) {
            "kapt" -> {
                add(name, "com.google.auto.service:auto-service")
            }

            "compileOnly" -> {
                add(name, "com.google.auto.service:auto-service-annotations")
            }

            "testImplementation" -> {
                add(name, "org.jetbrains.kotlin:kotlin-test")
                add(name, "org.spekframework.spek2:spek-dsl-jvm")
                add(name, "io.mockk:mockk")
            }

            "testRuntimeOnly" -> withDependencies {
                add(name, "org.spekframework.spek2:spek-runner-junit5")
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