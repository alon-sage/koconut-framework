plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

dependencies {
    api(project(":koconut-core"))
    api("io.grpc:grpc-api")

    implementation("io.grpc:grpc-core")
}

publishing {
    publications.register<MavenPublication>("jar") {
        from(components["java"])
    }
}
