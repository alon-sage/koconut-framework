package dev.koconut.framework.grpc.server

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.ProvidesIntoSet
import dev.koconut.framework.core.BlockingService
import dev.koconut.framework.core.Service
import dev.koconut.framework.core.config.ConfigBeans
import dev.koconut.framework.core.config.configBean
import io.grpc.BindableService
import io.grpc.InsecureServerCredentials
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import io.grpc.xds.XdsServerBuilder
import io.grpc.xds.XdsServerCredentials
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

@AutoService(Module::class)
class GrpcServerModule : AbstractModule() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun configure() {
        Multibinder.newSetBinder(binder(), BindableService::class.java)
    }

    @Provides
    @Singleton
    fun provideGrpcServerProperties(configBeans: ConfigBeans): GrpcServerProperties =
        configBeans.configBean("grpc.server")

    @Provides
    @Singleton
    fun provideXdsServerBuilder(properties: GrpcServerProperties): ServerBuilder<*> =
        XdsServerBuilder.forPort(
            properties.port,
            XdsServerCredentials.create(InsecureServerCredentials.create())
        )

    @Provides
    @Singleton
    fun provideGrpcServer(
        serverBuilder: ServerBuilder<*>,
        properties: GrpcServerProperties,
        services: Set<BindableService>
    ): Server =
        serverBuilder
            .let { services.fold(it) { builder, service -> builder.addService(service) } }
            .let { if (properties.enableReflection) it.addService(ProtoReflectionService.newInstance()) else it }
            .build()

    @ProvidesIntoSet
    @Singleton
    fun provideGrpcService(properties: GrpcServerProperties, server: Server): Service =
        BlockingService {
            logger.info("Starting GRPC server...")
            server.start()
            logger.info("GRPC server started at ${properties.port} port")

            BlockingService.Disposable {
                logger.info("Terminating GRPC server gracefully...")
                server.shutdown()
                if (!server.awaitTermination(properties.gracefulShutdownMillis, TimeUnit.MILLISECONDS)) {
                    logger.info("Force GRPC server termination")
                    server.shutdownNow()
                }
                logger.info("GRPC server terminated")
            }
        }
}

data class GrpcServerProperties(
    val port: Int,
    val gracefulShutdownMillis: Long,
    val enableReflection: Boolean
)

fun <V> SendChannel<V>.streamObserver(): StreamObserver<V> =
    object : StreamObserver<V> {
        override fun onNext(value: V) {
            trySendBlocking(value).getOrThrow()
        }

        override fun onError(t: Throwable) {
            close(t)
        }

        override fun onCompleted() {
            close()
        }
    }

suspend fun <V> Flow<V>.collect(streamObserver: StreamObserver<V>) =
    try {
        collect { streamObserver.onNext(it) }
        streamObserver.onCompleted()
    } catch (throwable: Throwable) {
        streamObserver.onError(throwable)
    }
