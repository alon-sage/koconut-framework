package com.google.inject.kotlin

import com.google.inject.internal.Errors
import com.google.inject.internal.KotlinSupportInterface
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Predicate
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty

@Suppress("unused")
object KotlinSupportImpl : KotlinSupportInterface {

    override fun getAnnotations(field: Field): Array<Annotation> =
        field.kotlinProperty?.annotations.orEmpty().toTypedArray()

    override fun isNullable(field: Field): Boolean =
        field.kotlinProperty?.returnType?.isMarkedNullable ?: false

    override fun getIsParameterKotlinNullablePredicate(constructor: Constructor<*>): Predicate<Int> {
        val parametersNullability = constructor
            .kotlinFunction
            ?.valueParameters
            ?.map { it.type.isMarkedNullable }
            ?: return Predicate { false }
        return Predicate { parametersNullability[it] }
    }

    override fun getIsParameterKotlinNullablePredicate(method: Method): Predicate<Int> {
        val parametersNullability = method
            .kotlinFunction
            ?.valueParameters
            ?.map { it.type.isMarkedNullable }
            ?: return Predicate { false }
        return Predicate { parametersNullability[it] }
    }

    override fun checkConstructorParameterAnnotations(constructor: Constructor<*>, errors: Errors) {}

    override fun isLocalClass(clazz: Class<*>): Boolean =
        clazz.isLocalClass
}