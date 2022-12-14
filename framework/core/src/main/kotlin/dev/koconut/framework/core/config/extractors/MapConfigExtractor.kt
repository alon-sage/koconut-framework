package dev.koconut.framework.core.config.extractors

import com.typesafe.config.Config
import com.typesafe.config.ConfigUtil
import dev.koconut.framework.core.Ordered
import dev.koconut.framework.core.config.AbstractConfigExtractor
import dev.koconut.framework.core.config.ConfigBeanFactory
import dev.koconut.framework.core.config.types.MapEntry
import kotlin.jvm.internal.TypeReference
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

class MapConfigExtractor(
    override val precedence: Int = DEFAULT_PRECEDENCE
) : AbstractConfigExtractor(), Ordered {

    override fun supports(type: KType): Boolean =
        type.classifier == Map::class

    override fun extract(config: Config, path: String, type: KType, configBeanFactory: ConfigBeanFactory): Any {
        val pathSplit = ConfigUtil.splitPath(path)
        val keyType = type.arguments[0].type!!
        val valueType = type.arguments[1].type!!

        return if (keyType.classifier == String::class) {
            config.getObject(path).keys.associateWith { key ->
                val valuePath = ConfigUtil.joinPath(pathSplit + key)
                configBeanFactory.configBean(config, valuePath, valueType)
            }
        } else {
            val listItemType = TypeReference(
                classifier = MapEntry::class,
                arguments = listOf(
                    KTypeProjection(KVariance.INVARIANT, keyType),
                    KTypeProjection(KVariance.INVARIANT, valueType)
                ),
                isMarkedNullable = false
            )
            val listType = TypeReference(
                classifier = List::class,
                arguments = listOf(
                    KTypeProjection(KVariance.INVARIANT, listItemType)
                ),
                isMarkedNullable = false
            )
            @Suppress("UNCHECKED_CAST")
            (configBeanFactory.configBean(config, path, listType) as List<MapEntry<*, *>>)
                .associate { it.key to it.value }
        }
    }

    companion object {
        const val DEFAULT_PRECEDENCE = 100
    }
}