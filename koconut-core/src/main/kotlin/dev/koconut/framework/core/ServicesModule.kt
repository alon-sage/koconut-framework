package dev.koconut.framework.core

import com.github.ajalt.clikt.core.CliktCommand
import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.ProvidesIntoSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

@AutoService(Module::class)
class ServicesModule : AbstractModule() {
    override fun configure() {
        Multibinder.newSetBinder(binder(), Service::class.java)
    }

    @Provides
    @Singleton
    fun provideServiceGroup(services: Set<Service>): ServiceGroup =
        DefaultServiceGroup(services.ordered())

    @ProvidesIntoSet
    @Singleton
    fun provideServeCommand(serviceGroup: ServiceGroup): CliktCommand =
        ServeCommand(serviceGroup)
}

fun interface Service {
    suspend fun start(): Disposable

    fun interface Disposable {
        suspend fun dispose()
    }
}

fun interface BlockingService : Service {
    fun startBlocking(): Disposable
    override suspend fun start(): Service.Disposable = withContext(Dispatchers.IO) { startBlocking() }

    fun interface Disposable : Service.Disposable {
        fun disposeBlocking()
        override suspend fun dispose() = withContext(Dispatchers.IO) { disposeBlocking() }
    }
}

interface ServiceGroup {
    suspend fun <T> use(
        gracefulShutdownMillis: Long = 30_000L,
        registerShutdownHook: Boolean = true,
        block: suspend () -> T
    ): T
}

class DefaultServiceGroup(
    private val services: List<Service>
) : ServiceGroup {
    override suspend fun <T> use(
        gracefulShutdownMillis: Long,
        registerShutdownHook: Boolean,
        block: suspend () -> T
    ): T = supervisorScope {
        var capturedThrowable: Throwable? = null

        val disposables = services
            .map { kotlin.runCatching { it.start() } }
            .onEach { if (it.isFailure) capturedThrowable = it.exceptionOrNull() }
            .takeWhile { it.isSuccess }
            .map { it.getOrThrow() }
            .reversed()

        suspend fun stopServices() {
            try {
                withContext(NonCancellable) {
                    withTimeout(gracefulShutdownMillis) {
                        disposables.forEach { it.dispose() }
                    }
                }
            } catch (e: Exception) {
                cancel("Graceful shutdown failed", e)
            }
        }

        capturedThrowable?.let { throwable ->
            try {
                stopServices()
                ensureActive()
            } catch (e: Exception) {
                throwable.addSuppressed(e)
            } finally {
                throw throwable
            }
        }

        var shutdownHook: Thread? = null
        if (registerShutdownHook) {
            shutdownHook = thread(start = false, name = "shutdownHook") { runBlocking { stopServices() } }
            Runtime.getRuntime().addShutdownHook(shutdownHook)
        }

        try {
            block()
        } finally {
            if (shutdownHook?.isAlive != true) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook)
                stopServices()
            } else {
                withContext(Dispatchers.IO) { shutdownHook.join() }
            }
        }
    }
}

class ServeCommand(
    private val serviceGroup: ServiceGroup
) : CliktCommand(
    name = "serve",
    help = "Run application services"
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run() {
        try {
            runBlocking { serviceGroup.use { awaitCancellation() } }
        } catch (e: Exception) {
            logger.error("Application failed", e)
        }
    }
}