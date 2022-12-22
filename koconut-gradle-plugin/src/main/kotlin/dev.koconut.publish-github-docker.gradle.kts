import com.google.cloud.tools.jib.gradle.BuildImageTask

plugins {
    id("com.google.cloud.tools.jib")
}

if (
    System.getenv("GITHUB_REPOSITORY") != null &&
    System.getenv("GITHUB_ACTOR") != null &&
    System.getenv("GITHUB_TOKEN") != null &&
    System.getenv("GITHUB_REF_TYPE") == "tag" &&
    System.getenv("GITHUB_REF_NAME") != null
) {
    tasks.register<BuildImageTask>("publishGitHubActions") {
        group = "jib"
        val image = buildString {
            append("ghcr.io/")
            append(System.getenv("GITHUB_REPOSITORY"))
            append(":")
            append(System.getenv("GITHUB_REF_NAME").removePrefix("v"))
        }
        setJibExtension(the())
        with(checkNotNull(jib)) {
            to.image = image
            to.auth.username = System.getenv("GITHUB_ACTOR")
            to.auth.password = System.getenv("GITHUB_TOKEN")
        }
    }
}