import java.net.URI

plugins {
    id("maven-publish")
}

if (
    runCatching { publishing }.isSuccess &&
    System.getenv("GITHUB_REPOSITORY") != null &&
    System.getenv("GITHUB_ACTOR") != null &&
    System.getenv("GITHUB_TOKEN") != null &&
    System.getenv("GITHUB_REF_TYPE") == "tag" &&
    System.getenv("GITHUB_REF_NAME") != null
) {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = URI.create("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications.withType<MavenPublication>() {
            version = System.getenv("GITHUB_REF_NAME").removePrefix("v")
        }
    }
}