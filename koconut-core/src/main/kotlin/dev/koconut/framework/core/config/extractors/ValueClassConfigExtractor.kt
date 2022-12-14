package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.ConfigBeanFactory
import dev.koconut.framework.core.config.ConfigExtractor
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class ValueClassConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : ConfigExtractor, Ordered {

    override fun tryExtract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any? {
        val clazz = type.classifier as? KClass<*> ?: return null
        if (!clazz.isValue) return null
        val constructor = clazz.primaryConstructor ?: return null

        return constructor.call(configBeanFactory.configBean(config, path, constructor.parameters[0].type))
    }

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}