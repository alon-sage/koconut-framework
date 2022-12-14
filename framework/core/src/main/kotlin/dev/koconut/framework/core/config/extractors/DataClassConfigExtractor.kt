package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigUtil
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.ConfigBeanFactory
import dev.koconut.framework.core.config.ConfigExtractor
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class DataClassConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : ConfigExtractor, Ordered {

    override fun tryExtract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any? {
        val clazz = type.classifier as? KClass<*> ?: return null
        if (!clazz.isData) return null
        val constructor = clazz.primaryConstructor ?: return null

        val pathSplit = ConfigUtil.splitPath(path)
        val parameters = constructor.parameters
            .mapNotNull {
                try {
                    it to configBeanFactory.configBean(config, ConfigUtil.joinPath(pathSplit + it.name), it.type)
                } catch (e: ConfigException.Missing) {
                    if (it.isOptional) null
                    else throw e
                }
            }
            .toMap()
        return constructor.callBy(parameters)
    }

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}