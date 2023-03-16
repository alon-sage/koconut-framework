plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

dependencies {
    api(project(":koconut-core"))
    api("io.ktor:ktor-server-core")
    api("io.ktor:ktor-client-core")

    implementation("io.ktor:ktor-server-test-host")
}

publishing {
    publications.register<MavenPublication>("jar") {
        from(components["java"])
    }
}
