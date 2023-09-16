import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    `java-platform`
    id("dev.koconut.build.publish-github-maven")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.jetbrains.kotlin:kotlin-bom:${getKotlinPluginVersion()}"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"))
    api(platform("com.google.inject:guice-bom:7.0.0"))
    api(platform("io.netty:netty-bom:4.1.97.Final"))
    api(platform("org.apache.logging.log4j:log4j-bom:2.20.0"))
    api(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))
    api(platform("com.google.protobuf:protobuf-bom:3.24.3"))
    api(platform("io.grpc:grpc-bom:1.58.0"))
    api(platform("io.ktor:ktor-bom:2.3.4"))
    api(platform("io.micrometer:micrometer-bom:1.11.4"))
    api(platform("io.opentelemetry:opentelemetry-bom:1.30.1"))
    api(platform("org.testcontainers:testcontainers-bom:1.19.0"))
    api(platform("org.junit:junit-bom:5.10.0"))

    constraints {
        api("com.google.auto.service:auto-service-annotations:1.0.1")
        api("com.google.auto.service:auto-service:1.0.1")

        api("com.typesafe:config:1.4.2")

        api("com.github.ajalt.clikt:clikt:4.2.0")

        api("org.slf4j:slf4j-api:2.0.9")
        api("org.slf4j:slf4j-simple:2.0.9")
        api("org.slf4j:jul-to-slf4j:2.0.9")
        api("ch.qos.logback:logback-core:1.4.11")
        api("ch.qos.logback:logback-classic:1.4.11")

        api("org.spekframework.spek2:spek-dsl-jvm:2.0.19")
        api("org.spekframework.spek2:spek-runner-junit5:2.0.19")
        api("io.mockk:mockk:1.13.7")

        api("com.google.protobuf:protoc:3.24.3")
        api("com.google.protobuf:protobuf-kotlin:3.24.3")
        api("io.grpc:grpc-kotlin-stub:1.3.1")
        api("io.grpc:protoc-gen-grpc-kotlin:1.3.1")
        api("com.google.protobuf:protobuf-gradle-plugin:0.9.4")

        api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    }
}

publishing {
    publications.register<MavenPublication>("javaPlatform") {
        from(components["javaPlatform"])
    }
}