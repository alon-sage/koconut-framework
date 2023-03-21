package dev.koconut.framework.graphql.server

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.multibindings.ProvidesIntoSet
import dev.koconut.framework.core.applicationInjector
import dev.koconut.framework.core.config.ConfigSource
import dev.koconut.framework.core.config.TestConfigSource
import dev.koconut.framework.core.getInstance
import graphql.ExecutionResult
import graphql.GraphQL
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertTrue

class GraphQLModuleRegularTest : Spek({
    val injector by memoized { applicationInjector { include(TestModule()) } }
    val graphql by memoized { injector.getInstance<GraphQL>() }

    describe("Request to server") {
        lateinit var result: ExecutionResult
        beforeEachTest { result = graphql.execute { it.query("query { test }") } }

        it("returns data") { assertTrue(result.isDataPresent) }
    }
}) {
    class TestModule : AbstractModule() {
        @ProvidesIntoSet
        @Singleton
        fun provideTestConfigSource(): ConfigSource =
            TestConfigSource(
                "graphql.server.federation.enabled" to "false",
                "graphql.server.schemaResources.0" to "graphql/regular.graphql"
            )
    }
}