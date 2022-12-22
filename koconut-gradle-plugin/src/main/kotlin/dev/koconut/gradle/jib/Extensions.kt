package dev.koconut.gradle.jib

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.add

fun DependencyHandler.jibJavaAgent(dependencyNotation: Any): Dependency? =
    add("jibJavaAgent", dependencyNotation)

fun DependencyHandler.jibJavaAgent(
    dependencyNotation: String,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): ExternalModuleDependency =
    add("jibJavaAgent", dependencyNotation, dependencyConfiguration)