plugins {
    id("com.google.cloud.tools.jib")
}

val jibJavaAgent by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

jib {
    to { image = "${findProperty("jib.to.registry.url") ?: ""}${project.name}:${project.version}" }
}

afterEvaluate {
    jib {
        val agentsJars = jibJavaAgent.resolve()
        extraDirectories { setPaths(agentsJars.map { it.parentFile }) }
        container { jvmFlags = jvmFlags + agentsJars.map { "-javaagent:${it.name}" } }
    }
}