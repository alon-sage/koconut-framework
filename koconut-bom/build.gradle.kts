plugins {
    `java-platform`
    id("dev.koconut.build.publish-github-maven")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(project(":koconut-dependencies")))
    constraints {
        api(project(":koconut-core"))
        api(project(":koconut-ktor-server"))
        api(project(":koconut-ktor-server-test"))
    }
}

publishing {
    publications.register<MavenPublication>("javaPlatform") {
        from(components["javaPlatform"])
    }
}