import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.assertDoesNotThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Files
import kotlin.test.assertNotNull

class DevKoconutJibGradlePluginTest : Spek({
    val testProjectDir by memoized(
        factory = { Files.createTempDirectory("gradle-test").toFile() },
        destructor = { it.deleteRecursively() }
    )
    beforeEachTest {
        testProjectDir
            .resolve("build.gradle.kts")
            .writeText(
                """
                plugins {
                    java
                    id("dev.koconut.jib")
                }
                repositories {
                    mavenCentral()
                }
                dependencies {
                    jibJavaAgent("io.opentelemetry.javaagent:opentelemetry-javaagent:1.21.0")
                }
                """.trimIndent()
            )
    }

    val runner by memoized {
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
    }
    describe("build") {
        var result: Result<BuildResult>? = null
        beforeEachTest { result = runCatching { runner.build() } }
        it("call happens") { assertNotNull(result) }
        it("completes successfully") { assertDoesNotThrow { result!!.getOrThrow() } }
    }
})