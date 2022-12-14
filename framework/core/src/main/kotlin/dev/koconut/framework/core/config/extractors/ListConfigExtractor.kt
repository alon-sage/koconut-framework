package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import com.typesafe.config.ConfigUtil
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.AbstractConfigExtractor
import dev.koconut.framework.core.config.ConfigBeanFactory
import kotlin.reflect.KType

class ListConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : AbstractConfigExtractor(), Ordered {

    override fun supports(type: KType): Boolean =
        type.classifier == List::class

    override fun extract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any {
        val pathSplit = ConfigUtil.splitPath(path)
        val itemType = type.arguments[0].type!!
        return config.getList(path).mapIndexed { index, item ->
            val itemPath = ConfigUtil.joinPath(pathSplit + index.toString())
            configBeanFactory.configBean(item.atPath(itemPath), itemPath, itemType)
        }
    }

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}