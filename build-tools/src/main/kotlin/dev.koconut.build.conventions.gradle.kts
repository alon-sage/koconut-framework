import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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

tasks.withType<KotlinCompilationTask<*>> {
    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-java-parameters", "-Xjvm-default=all"))
    }
}

tasks.withType<KotlinCompilationTask<KotlinJvmCompilerOptions>> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.withType<Test> {
    useJUnitPlatform { includeEngines("spek2") }
    reports {
        junitXml.required.set(true)
    }
}

afterEvaluate {
    extensions
        .findByType<JavaPluginExtension>()
        ?.apply {
            targetCompatibility = JavaVersion.VERSION_1_8
            sourceCompatibility = JavaVersion.VERSION_1_8
        }
}