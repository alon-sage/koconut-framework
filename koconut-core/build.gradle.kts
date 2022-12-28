plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
    api("com.google.inject:guice")
    api("com.github.ajalt.clikt:clikt")
    api("com.typesafe:config")
    api("org.slf4j:slf4j-api")
    api("io.micrometer:micrometer-core")
    api("io.opentelemetry:opentelemetry-api")
    api("io.opentelemetry:opentelemetry-extension-kotlin")

    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
    implementation("org.apache.logging.log4j:log4j-to-slf4j")
    implementation("org.slf4j:jul-to-slf4j")
}

publishing {
    publications.register<MavenPublication>("jar") {
        from(components["java"])
    }
}