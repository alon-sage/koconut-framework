package dev.koconut.framework.grpc.server

import dev.koconut.framework.core.ServiceGroup
import dev.koconut.framework.core.applicationInjector
import dev.koconut.framework.core.getInstance
import dev.koconut.framework.grpc.server.test.TestGrpcServerModule
import dev.koconut.framework.grpc.server.test.getTestGrpcChannel
import io.grpc.health.v1.HealthCheckRequest
import io.grpc.health.v1.HealthCheckResponse
import io.grpc.health.v1.HealthCheckResponse.ServingStatus
import io.grpc.health.v1.HealthGrpc
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.single
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GrpcServerModuleTest : Spek({
    val injector by memoized { applicationInjector { override(TestGrpcServerModule()) } }
    val serviceGroup by memoized { injector.getInstance<ServiceGroup>() }
    val channel by memoized(factory = { injector.getTestGrpcChannel() }, destructor = { it.shutdown() })
    val health by memoized { HealthGrpc.newStub(channel) }
    val request by memoized { HealthCheckRequest.getDefaultInstance() }

    describe("Request to server") {
        var responses: Result<HealthCheckResponse>? = null
        beforeEachTest {
            responses = runCatching {
                serviceGroup.useBlocking {
                    channelFlow {
                        health.check(request, streamObserver())
                        awaitClose()
                    }
                        .single()
                }
            }
        }
        it("call happens") { assertNotNull(responses) }
        it("returns status") { assertEquals(ServingStatus.SERVING, responses!!.getOrThrow().status) }
    }
})