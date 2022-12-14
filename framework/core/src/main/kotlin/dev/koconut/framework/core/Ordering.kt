package dev.koconut.framework.core

const val MAX_PRECEDENCE = Int.MIN_VALUE

const val MIN_PRECEDENCE = Int.MAX_VALUE

const val DEFAULT_PRECEDENCE = 0

fun <T> Iterable<T>.ordered(): List<T> = sortedBy { orderOf(it) }

fun orderOf(value: Any?): Int = (value as? Ordered)?.precedence ?: DEFAULT_PRECEDENCE

interface Ordered {
    val precedence: Int
}

