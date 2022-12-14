package dev.koconut.framework.core.config

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.ProvisionException
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.ProvidesIntoSet
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import dev.koconut.framework.core.ordered
import dev.koconut.framework.core.config.extractors.ArrayConfigExtractor
import dev.koconut.framework.core.config.extractors.BooleanConfigExtractor
import dev.koconut.framework.core.config.extractors.ClassConfigExtractor
import dev.koconut.framework.core.config.extractors.DataClassConfigExtractor
import dev.koconut.framework.core.config.extractors.DurationConfigExtractor
import dev.koconut.framework.core.config.extractors.EnumClassConfigExtractor
import dev.koconut.framework.core.config.extractors.ListConfigExtractor
import dev.koconut.framework.core.config.extractors.MapConfigExtractor
import dev.koconut.framework.core.config.extractors.NumberConfigExtractor
import dev.koconut.framework.core.config.extractors.StringConfigExtractor
import dev.koconut.framework.core.config.extractors.ValueClassConfigExtractor
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface ConfigExtractor {
    fun tryExtract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any?
}

abstract class AbstractConfigExtractor : ConfigExtractor {

    override fun tryExtract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any? =
        if (supports(type)) extract(config, path, type, configBeanFactory) else null

    protected abstract fun supports(
        type: KType
    ): Boolean

    protected abstract fun extract(
        config: Config,
        path: String,
        type: KType,
        configBeanFactory: ConfigBeanFactory
    ): Any
}

interface ConfigBeanFactory {
    fun configBean(config: Config, path: String, beanType: KType): Any?
}

class DefaultConfigBeanFactory(
    private val extractors: List<ConfigExtractor>
) : ConfigBeanFactory {

    constructor(extractors: Set<ConfigExtractor>) : this(extractors.ordered())

    override fun configBean(config: Config, path: String, beanType: KType): Any? =
        try {
            config.getValue(path)
            extractors
                .firstNotNullOfOrNull { extractor ->
                    try {
                        extractor.tryExtract(config, path, beanType, this)
                    } catch (e: ConfigException) {
                        throw e
                    } catch (e: Exception) {
                        throw ConfigException.BugOrBroken("Configuration key '$path' decoding failed.", e)
                    }
                }
                ?: throw ConfigException.BadValue(
                    config.origin(),
                    path,
                    "Configuration key '$path' can not be decoded as $beanType."
                )
        } catch (e: ConfigException.Missing) {
            if (beanType.isMarkedNullable) null
            else throw e
        }
}

interface ConfigBeans {
    fun configBean(path: String, beanType: KType): Any?

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> requiredConfigBean(path: String, beanType: KClass<T>): T =
        configBean(path, Reflection.typeOf(beanType)) as T

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> optionalConfigBean(path: String, beanType: KClass<T>): T? =
        configBean(path, Reflection.nullableTypeOf(beanType)) as T
}

inline fun <reified T> ConfigBeans.configBean(path: String): T =
    configBean(path, typeOf<T>()) as T

class DefaultConfigBeans(
    private val config: Config,
    private val configBeanFactory: ConfigBeanFactory
) : ConfigBeans {

    override fun configBean(path: String, beanType: KType): Any? =
        try {
            configBeanFactory.configBean(config, path, beanType)
        } catch (e: ConfigException) {
            throw ProvisionException(e.message, e)
        }
}

@AutoService(Module::class)
class ConfigBeanModule : AbstractModule() {

    override fun configure() {
        Multibinder.newSetBinder(binder(), ConfigExtractor::class.java)
    }

    @ProvidesIntoSet
    @Singleton
    fun provideArrayConfigExtractor(): ConfigExtractor =
        ArrayConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideBooleanConfigExtractor(): ConfigExtractor =
        BooleanConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideClassConfigExtractor(): ConfigExtractor =
        ClassConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideDataClassConfigExtractor(): ConfigExtractor =
        DataClassConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideDurationConfigExtractor(): ConfigExtractor =
        DurationConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideEnumClassConfigExtractor(): ConfigExtractor =
        EnumClassConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideListConfigExtractor(): ConfigExtractor =
        ListConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideMapConfigExtractor(): ConfigExtractor =
        MapConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideNumberConfigExtractor(): ConfigExtractor =
        NumberConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideStringConfigExtractor(): ConfigExtractor =
        StringConfigExtractor()

    @ProvidesIntoSet
    @Singleton
    fun provideValueClassConfigExtractor(): ConfigExtractor =
        ValueClassConfigExtractor()

    @Provides
    @Singleton
    fun provideConfigBeanFactory(extractors: Set<ConfigExtractor>): ConfigBeanFactory =
        DefaultConfigBeanFactory(extractors)

    @Provides
    @Singleton
    fun provideConfigBeans(config: Config, configBeanFactory: ConfigBeanFactory): ConfigBeans =
        DefaultConfigBeans(config, configBeanFactory)

}