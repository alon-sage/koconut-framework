plugins {
    `java-platform`
    id("dev.koconut.build.publish-github-maven")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.7.22"))
    api(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    api(enforcedPlatform("com.google.inject:guice-bom:5.1.0"))
    api(enforcedPlatform("io.netty:netty-bom:4.1.85.Final"))
    api(enforcedPlatform("org.apache.logging.log4j:log4j-bom:2.19.0"))
    api(enforcedPlatform("com.fasterxml.jackson:jackson-bom:2.14.1"))
    api(enforcedPlatform("io.micrometer:micrometer-bom:1.10.2"))
    api(enforcedPlatform("io.opentelemetry:opentelemetry-bom:1.20.1"))
    api(enforcedPlatform("org.testcontainers:testcontainers-bom:1.17.6"))

    constraints {
        api("com.google.auto.service:auto-service-annotations:1.0.1")
        api("com.google.auto.service:auto-service:1.0.1")

        api("com.typesafe:config:1.4.2")

        api("com.github.ajalt.clikt:clikt:3.5.0")

        api("org.slf4j:slf4j-api:2.0.5")
        api("org.slf4j:slf4j-simple:2.0.5")
        api("org.slf4j:jul-to-slf4j:2.0.5")
        api("net.logstash.logback:logstash-logback-encoder:7.2")
        api("org.codehaus.janino:janino:3.1.9")
        api("ch.qos.logback:logback-core:1.4.5")
        api("ch.qos.logback:logback-classic:1.4.5")

        api("io.opentelemetry.javaagent:opentelemetry-javaagent:1.20.2")

        api("org.spekframework.spek2:spek-dsl-jvm:2.0.19")
        api("org.spekframework.spek2:spek-runner-junit5:2.0.19")
        api("io.mockk:mockk:1.13.3")
    }
}

tasks.withType<GenerateModuleMetadata> {
    @Suppress("UnstableApiUsage")
    suppressedValidationErrors.add("enforced-platform")
}

publishing {
    publications.register<MavenPublication>("pom") {
        from(components["javaPlatform"])
    }
}