package dev.koconut.framework.core.config.types

@JvmInline
value class Secret(val value: String) {
    override fun toString(): String = "********"
}