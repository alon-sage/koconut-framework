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
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
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
    fun provideGrpcServer(properties: GrpcServerProperties, services: Set<BindableService>): Server =
        ServerBuilder
            .forPort(properties.port)
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
