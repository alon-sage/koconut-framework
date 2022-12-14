plugins {
    id("dev.koconut.build.conventions") apply false
    id("dev.koconut.build.publish-github-maven") apply false
}

allprojects {
    group = "dev.koconut.framework"
    version = "0.0.1-SNAPSHOT"
}