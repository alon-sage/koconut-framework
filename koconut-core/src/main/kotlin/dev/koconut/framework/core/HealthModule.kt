package dev.koconut.framework.core

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder

@AutoService(Module::class)
class HealthModule : AbstractModule() {
    override fun configure() {
        Multibinder.newSetBinder(binder(), HealthCheck::class.java)
    }

    @Provides
    @Singleton
    fun provideDefaultHealthManager(checks: Set<HealthCheck>): HealthManager =
        DefaultHealthManager(checks.ordered())
}

interface HealthManager {
    fun healthState(): HealthState
}

class DefaultHealthManager(
    private val checks: List<HealthCheck>
) : HealthManager {

    override fun healthState(): HealthState {
        val results = checks.associate {
            it.name to try {
                it.checkResult()
            } catch (e: Exception) {
                HealthCheckResult(false, e.stackTraceToString())
            }
        }
        val success = results.values.all { it.success }
        val reason = if (success) "All checks completed successfully" else "Some checks failed"
        return HealthState(success, reason, results)
    }
}

interface HealthCheck {
    val name: String
    fun checkResult(): HealthCheckResult
}

class NamedHealthCheck(
    override val name: String,
    private val block: () -> HealthCheckResult
) : HealthCheck {
    override fun checkResult(): HealthCheckResult = block()
}

data class HealthCheckResult(
    val success: Boolean,
    val reason: String
)

data class HealthState(
    val success: Boolean,
    val reason: String,
    val checks: Map<String, HealthCheckResult>
)