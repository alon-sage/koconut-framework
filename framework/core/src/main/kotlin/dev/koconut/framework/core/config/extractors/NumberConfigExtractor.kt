package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.AbstractConfigExtractor
import dev.koconut.framework.core.config.ConfigBeanFactory
import kotlin.reflect.KType

class NumberConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : AbstractConfigExtractor(), Ordered {

    override fun supports(type: KType): Boolean =
        when (type.classifier) {
            Number::class -> true
            Byte::class -> true
            Short::class -> true
            Int::class -> true
            Long::class -> true
            Float::class -> true
            Double::class -> true
            else -> false
        }

    override fun extract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any =
        when (type.classifier) {
            Number::class -> config.getNumber(path)
            Byte::class -> config.getInt(path).toByte()
            Short::class -> config.getInt(path).toShort()
            Int::class -> config.getInt(path)
            Long::class -> config.getLong(path)
            Float::class -> config.getDouble(path).toFloat()
            Double::class -> config.getDouble(path)
            else -> error("Should never rich this branch")
        }

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}