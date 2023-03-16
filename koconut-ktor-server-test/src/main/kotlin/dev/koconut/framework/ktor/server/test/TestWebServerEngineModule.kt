package dev.koconut.framework.ktor.server.test

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import dev.koconut.framework.core.getInstance
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.client.TestHttpClientEngine

class TestWebServerEngineModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideTestApplicationEngine(
        environment: ApplicationEngineEnvironment
    ): TestApplicationEngine =
        TestApplicationEngine(environment)

    @Provides
    @Singleton
    fun provideApplicationEngine(
        engine: TestApplicationEngine
    ): ApplicationEngine =
        engine
}

fun Injector.getTestClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit = {}): HttpClient =
    HttpClient(TestHttpClientEngine) {
        engine { app = getInstance() }
        block()
    }