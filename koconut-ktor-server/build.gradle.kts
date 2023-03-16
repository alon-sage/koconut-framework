plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

dependencies {
    api(project(":koconut-core"))
    api("io.ktor:ktor-server-core")
    api("io.ktor:ktor-server-auth")
    api("io.ktor:ktor-server-content-negotiation")
    api("io.ktor:ktor-server-status-pages")

    implementation("io.ktor:ktor-server-host-common")

    testImplementation(project(":koconut-ktor-server-test"))
}

publishing {
    publications.register<MavenPublication>("jar") {
        from(components["java"])
    }
}
