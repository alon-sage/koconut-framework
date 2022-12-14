package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.AbstractConfigExtractor
import dev.koconut.framework.core.config.ConfigBeanFactory
import java.time.Duration
import kotlin.reflect.KType

class DurationConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : AbstractConfigExtractor(), Ordered {

    override fun supports(type: KType): Boolean =
        type.classifier == Duration::class

    override fun extract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any =
        config.getDuration(path)

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}