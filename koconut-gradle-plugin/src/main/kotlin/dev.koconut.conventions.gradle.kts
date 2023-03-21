import dev.koconut.gradle.asm.MainClassFinder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

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

plugins.withType<JavaPlugin> {
    configure<JavaPluginExtension> {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
}

plugins.withType<ApplicationPlugin> {
    configure<JavaApplication> {
        mainClass.convention(resolvedMainClass)
    }
}

afterEvaluate {
    resolvedMainClass?.let { mainClass ->
        tasks.named<Jar>("jar") {
            manifest {
                attributes("Main-Class" to mainClass)
            }
        }
    }
}