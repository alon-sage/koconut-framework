import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

repositories {
    mavenCentral()
}

val koconutBomSpec = loadPropertyFromResources("bom-spec.properties", "koconut-bom")

with(project.dependencies) {
    configurations.all {
        withDependencies {
            add(platform(koconutBomSpec))
        }
        when (name) {
            "kapt" -> withDependencies {
                add(create("com.google.auto.service:auto-service"))
            }

            "testImplementation" -> withDependencies {
                add(create("org.jetbrains.kotlin:kotlin-test"))
                add(create("org.spekframework.spek2:spek-dsl-jvm"))
                add(create("io.mockk:mockk"))
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