import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(platform("org.jetbrains.kotlin:kotlin-bom:1.8.10"))
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict", "-java-parameters", "-Xjvm-default=all")
    }
}