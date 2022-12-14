package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.AbstractConfigExtractor
import dev.koconut.framework.core.config.ConfigBeanFactory
import kotlin.reflect.KType

class ClassConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : AbstractConfigExtractor(), Ordered {

    override fun supports(type: KType): Boolean =
        type.classifier == Class::class

    override fun extract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any =
        Thread.currentThread().contextClassLoader.loadClass(config.getString(path))

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}