plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("dev.koconut.build.conventions")
    id("dev.koconut.build.publish-github-maven")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")
    compileOnly("com.google.protobuf:protobuf-gradle-plugin")
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

val bomSpec = provider {
    project(":koconut-bom")
        .publishing
        .publications["javaPlatform"]
        .let { it as MavenPublication }
        .run { "$groupId:$artifactId:$version" }
}

val bomSpecFile = layout.buildDirectory.file("bom-spec.properties")

val generateBomSpecFile by tasks.registering {
    inputs.property("bomSpec", bomSpec)
    outputs.file(bomSpecFile).withPropertyName("bomSpecFile")
    doLast { bomSpecFile.get().asFile.writeText("koconut-bom=${bomSpec.get()}") }
}

tasks.processResources {
    dependsOn(generateBomSpecFile)
    from(bomSpecFile)
}
