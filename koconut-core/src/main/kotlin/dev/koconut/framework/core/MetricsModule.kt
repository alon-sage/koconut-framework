package dev.koconut.framework.core

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.ProvidesIntoSet
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmCompilationMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics

@AutoService(Module::class)
class MetricsModule : AbstractModule() {
    override fun configure() {
        Multibinder.newSetBinder(binder(), MeterBinder::class.java)
    }

    @Provides
    @Singleton
    fun provideMeterRegistry(binders: Set<MeterBinder>): MeterRegistry =
        Metrics.globalRegistry
            .apply { binders.forEach { it.bindTo(this) } }

    @ProvidesIntoSet
    @Singleton
    fun provideUptimeMetrics(): MeterBinder =
        UptimeMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideProcessorMetrics(): MeterBinder =
        ProcessorMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideFileDescriptorMetrics(): MeterBinder =
        FileDescriptorMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideClassLoaderMetrics(): MeterBinder =
        ClassLoaderMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideJvmCompilationMetrics(): MeterBinder =
        JvmCompilationMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideJvmGcMetrics(): MeterBinder =
        JvmGcMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideJvmHeapPressureMetrics(): MeterBinder =
        JvmHeapPressureMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideJvmInfoMetrics(): MeterBinder =
        JvmInfoMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideJvmMemoryMetrics(): MeterBinder =
        JvmMemoryMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideJvmThreadMetrics(): MeterBinder =
        JvmThreadMetrics()

    @ProvidesIntoSet
    @Singleton
    fun provideLogbackMetrics(): MeterBinder =
        LogbackMetrics()
}