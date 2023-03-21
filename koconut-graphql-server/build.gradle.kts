plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

dependencies {
    api(project(":koconut-core"))
    api("com.graphql-java:graphql-java")
    api("com.graphql-java:graphql-java-extended-scalars")
    api("com.graphql-java:graphql-java-extended-validation")
    api("com.graphql-java:java-dataloader")
    api("com.apollographql.federation:federation-graphql-java-support")
}

publishing {
    publications.register<MavenPublication>("jar") {
        from(components["java"])
    }
}
