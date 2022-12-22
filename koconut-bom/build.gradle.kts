plugins {
    `java-platform`
    id("dev.koconut.build.publish-github-maven")
}

javaPlatform {
    allowDependencies()
}

val dependencyProject = project(":koconut-dependencies")

configurations.api {
    withDependencies {
        dependencies.addAllLater(dependencyProject.configurations.api.map { it.dependencies })
        dependencyConstraints.addAllLater(dependencyProject.configurations.api.map { it.dependencyConstraints })
    }
}

dependencies {
    constraints {
        api(project(":koconut-core"))
        api(project(":koconut-ktor-server"))
        api(project(":koconut-ktor-server-test"))
        api(project(":koconut-grpc-server"))
    }
}

publishing {
    publications.register<MavenPublication>("javaPlatform") {
        from(components["javaPlatform"])
    }
}