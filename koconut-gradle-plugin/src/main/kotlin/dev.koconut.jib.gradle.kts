plugins {
    id("com.google.cloud.tools.jib")
}

val jibJavaAgent by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

jib {
    to { image = project.name }
}

afterEvaluate {
    jib {
        val agentsJars = jibJavaAgent.resolve()
        extraDirectories { setPaths(agentsJars.map { it.parentFile }) }
        container { jvmFlags = jvmFlags + agentsJars.map { "-javaagent:${it.name}" } }
    }
}
