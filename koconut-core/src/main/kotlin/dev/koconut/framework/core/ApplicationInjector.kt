package dev.koconut.framework.core

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.Stage
import com.google.inject.TypeLiteral
import com.google.inject.util.Modules
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

fun applicationInjector(configurer: InjectorConfiguration.() -> Unit = {}): Injector {
    val configuration = InjectorConfigurationImpl().apply(configurer)
    val resolvedModules = ServiceLoader.load(Module::class.java)
        .filter { it::class !in configuration.excluded }
        .plus(configuration.included)
    return if (configuration.overrides.isNotEmpty()) {
        val overridenModules = Modules.override(resolvedModules).with(configuration.overrides)
        Guice.createInjector(configuration.stage, overridenModules)
    } else {
        Guice.createInjector(configuration.stage, resolvedModules)
    }
}

interface InjectorConfiguration {
    fun stage(value: Stage)
    fun include(module: Module)
    fun exclude(moduleClass: KClass<out Module>)
    fun override(module: Module)
}

internal class InjectorConfigurationImpl : InjectorConfiguration {
    var stage: Stage = Stage.DEVELOPMENT
    val included = mutableListOf<Module>()
    val excluded = mutableSetOf<KClass<out Module>>()
    val overrides = mutableListOf<Module>()

    override fun stage(value: Stage) {
        stage = value
    }

    override fun include(module: Module) {
        included.add(module)
    }

    override fun exclude(moduleClass: KClass<out Module>) {
        excluded.add(moduleClass)
    }

    override fun override(module: Module) {
        overrides.add(module)
    }
}

inline fun <reified T> Injector.getInstance(): T {
    val typeLiteral = TypeLiteral.get(typeOf<T>().javaType)
    val key = Key.get(typeLiteral)
    return getInstance(key) as T
}

inline fun <reified T> Injector.getInstance(annotation: Annotation): T {
    val typeLiteral = TypeLiteral.get(typeOf<T>().javaType)
    val key = Key.get(typeLiteral, annotation)
    return getInstance(key) as T
}

inline fun <reified T> Injector.getInstance(annotationClass: KClass<out Annotation>): T {
    val typeLiteral = TypeLiteral.get(typeOf<T>().javaType)
    val key = Key.get(typeLiteral, annotationClass.java)
    return getInstance(key) as T
}

inline fun <reified T> typeLiteralOf(): TypeLiteral<T> =
    object : TypeLiteral<T>() {}
