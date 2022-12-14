package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.AbstractConfigExtractor
import dev.koconut.framework.core.config.ConfigBeanFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType

class EnumClassConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : AbstractConfigExtractor(), Ordered {

    override fun supports(type: KType): Boolean =
        (type.classifier as? KClass<*>)?.java?.isEnum == true

    @Suppress("UNCHECKED_CAST")
    override fun extract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any =
        config.getEnum((type.classifier as KClass<out Enum<*>>).java, path)

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}