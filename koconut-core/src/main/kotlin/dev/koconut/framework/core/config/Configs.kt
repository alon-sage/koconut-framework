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
import com.typesafe.config.ConfigFactory
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.ordered

@AutoService(Module::class)
class ConfigModule : AbstractModule() {

    override fun configure() {
        Multibinder.newSetBinder(binder(), ConfigSource::class.java)
    }

    @ProvidesIntoSet
    @Singleton
    fun provideReferenceConfigSource(): ConfigSource =
        OrderedConfigSource(REFERENCE_CONFIG_PRECEDENCE) { ConfigFactory.defaultReferenceUnresolved() }

    @ProvidesIntoSet
    @Singleton
    fun provideDefaultApplicationConfigSource(): ConfigSource =
        OrderedConfigSource(DEFAULT_APPLICATION_CONFIG_PRECEDENCE) { ConfigFactory.defaultApplication() }

    @ProvidesIntoSet
    @Singleton
    fun provideSystemPropertiesConfigSource(): ConfigSource =
        OrderedConfigSource(SYSTEM_PROPERTIES_CONFIG_PRECEDENCE) { ConfigFactory.systemProperties() }

    @ProvidesIntoSet
    @Singleton
    fun provideSystemEnvironmentOverridesConfigSource(): ConfigSource =
        OrderedConfigSource(SYSTEM_ENVIRONMENT_CONFIG_PRECEDENCE) { ConfigFactory.systemEnvironmentOverrides() }

    @Provides
    @Singleton
    fun provideConfig(sources: Set<ConfigSource>): Config =
        try {
            sources
                .ordered()
                .map { it.config() }
                .reduceOrNull(Config::withFallback)
                ?.resolve()
                ?: ConfigFactory.empty()
        } catch (e: ConfigException) {
            throw ProvisionException(e.message, e)
        }
}

const val REFERENCE_CONFIG_PRECEDENCE = Int.MAX_VALUE
const val DEFAULT_APPLICATION_CONFIG_PRECEDENCE = 1200
const val SYSTEM_PROPERTIES_CONFIG_PRECEDENCE = 1100
const val SYSTEM_ENVIRONMENT_CONFIG_PRECEDENCE = 1000

fun interface ConfigSource {
    fun config(): Config
}

class OrderedConfigSource(
    override val precedence: Int,
    block: () -> Config
) : Ordered, ConfigSource by ConfigSource({ block() })

class TestConfigSource(
    private val values: Map<String, Any?>
) : Ordered, ConfigSource {

    constructor(vararg values: Pair<String, Any?>) : this(values.toMap())

    override val precedence: Int = Int.MIN_VALUE
    override fun config(): Config = ConfigFactory.parseMap(values)
}
