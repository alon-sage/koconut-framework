package dev.koconut.framework.core

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry

@AutoService(Module::class)
class OpenTelemetryModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideOpenTelemetry(): OpenTelemetry =
        GlobalOpenTelemetry.get()
}