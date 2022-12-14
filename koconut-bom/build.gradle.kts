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
    }
}

publishing {
    publications.register<MavenPublication>("pom") {
        from(components["javaPlatform"])
    }
}