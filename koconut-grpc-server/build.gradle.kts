plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

dependencies {
    api(project(":koconut-core"))
    api("io.grpc:grpc-api")
    api("io.grpc:grpc-netty")
    implementation("io.grpc:grpc-services")
}

publishing {
    publications.register<MavenPublication>("jar") {
        from(components["java"])
    }
}
