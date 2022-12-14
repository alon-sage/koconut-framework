plugins {
    `maven-publish`
}

publishing {
    if (
        System.getenv("GITHUB_REPOSITORY") != null &&
        System.getenv("GITHUB_ACTOR") != null &&
        System.getenv("GITHUB_TOKEN") != null &&
        System.getenv("GITHUB_REF_TYPE") == "tag" &&
        System.getenv("GITHUB_REF_NAME") != null
    ) {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
    publications.withType<MavenPublication>().all {
        System.getenv("GITHUB_REPOSITORY")?.let { pom { url.set("https://github.com/$it.git") } }
        System.getenv("GITHUB_REF_NAME")?.let { version = it.removePrefix("v") }
    }
}
