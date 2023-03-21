package dev.koconut.framework.ktor.server

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.multibindings.ProvidesIntoSet
import dev.koconut.framework.core.applicationInjector
import dev.koconut.framework.ktor.server.test.TestWebServerEngineModule
import dev.koconut.framework.ktor.server.test.getTestClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

class KtorWebServerModuleTest : Spek({
    val injector by memoized {
        applicationInjector {
            override(TestWebServerEngineModule())
            include(TestModule())
        }
    }
    val client by memoized { injector.getTestClient() }

    describe("Request to server") {
        lateinit var response: HttpResponse
        beforeEachTest { runBlocking { response = client.get("/") } }
        it("returns 200") { assertEquals(200, response.status.value) }

        lateinit var bodyText: String
        beforeEachTest { runBlocking { bodyText = response.bodyAsText() } }
        it("returns body") { assertEquals("Lorem ipsum dolores", bodyText) }
    }
}) {
    class TestModule : AbstractModule() {
        @ProvidesIntoSet
        @Singleton
        fun provideTestRoute(): WebServerConfigurer =
            WebServerConfigurer {
                routing {
                    get("/") { context.respondText("Lorem ipsum dolores") }
                }
            }
    }
}