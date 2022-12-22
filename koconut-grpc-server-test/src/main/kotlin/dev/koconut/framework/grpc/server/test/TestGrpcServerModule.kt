package dev.koconut.framework.grpc.server.test

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import dev.koconut.framework.core.getInstance
import io.grpc.ManagedChannel
import io.grpc.ServerBuilder
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder

class TestGrpcServerModule : AbstractModule() {
    @Provides
    @Singleton
    @Named("grpcTestServerName")
    fun provideTestServerName(): String =
        InProcessServerBuilder.generateName()

    @Provides
    @Singleton
    fun provideTestServerBuilder(
        @Named("grpcTestServerName") testServerName: String
    ): ServerBuilder<*> =
        InProcessServerBuilder.forName(testServerName)
}

fun Injector.getTestGrpcChannel(): ManagedChannel =
    InProcessChannelBuilder.forName(getInstance(Named("grpcTestServerName"))).build()