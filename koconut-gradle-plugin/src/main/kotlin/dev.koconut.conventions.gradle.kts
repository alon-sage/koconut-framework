import dev.koconut.gradle.asm.MainClassFinder
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

repositories {
    mavenCentral()
}

val koconutBomSpec = loadPropertyFromResources("bom-spec.properties", "koconut-bom")

with(dependencies) {
    configurations.all {
        withDependencies {
            add(platform(koconutBomSpec))
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

val resolvedMainClass by lazy {
    extensions
        .getByType<SourceSetContainer>()
        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        .output
        .filter { it.isDirectory }
        .files
        .flatMap { MainClassFinder.mainClasses(it) }
        .singleOrNull()
}

afterEvaluate {
    extensions
        .findByType<JavaPluginExtension>()
        ?.apply {
            targetCompatibility = JavaVersion.VERSION_11
            sourceCompatibility = JavaVersion.VERSION_11
        }

    extensions
        .findByType<JavaApplication>()
        ?.apply { mainClass.convention(resolvedMainClass) }

    resolvedMainClass?.let { mainClass ->
        tasks.named<Jar>("jar") {
            manifest {
                attributes("Main-Class" to mainClass)
            }
        }
    }
}