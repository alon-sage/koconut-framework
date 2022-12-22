package dev.koconut.framework.ktor.server

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.ProvidesIntoSet
import com.typesafe.config.Config
import dev.koconut.framework.core.BlockingService
import dev.koconut.framework.core.Service
import dev.koconut.framework.core.config.ConfigBeans
import dev.koconut.framework.core.config.configBean
import dev.koconut.framework.core.config.types.Secret
import dev.koconut.framework.core.ordered
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.ConnectorType
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.sslConnector
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

@AutoService(Module::class)
class KtorWebServerModule : AbstractModule() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun configure() {
        Multibinder.newSetBinder(binder(), WebServerConfigurer::class.java)
    }

    @Provides
    @Singleton
    fun provideKtorDeploymentProperties(configBeans: ConfigBeans): KtorDeploymentProperties =
        configBeans.configBean("ktor.deployment")

    @Provides
    @Singleton
    fun provideKtorSslProperties(configBeans: ConfigBeans): KtorSslProperties? =
        configBeans.configBean("ktor.security.ssl")

    @Provides
    @Singleton
    fun provideHoconApplicationConfig(config: Config): ApplicationConfig =
        HoconApplicationConfig(config.withOnlyPath("ktor"))

    @Provides
    @Singleton
    fun provideApplicationEnvironment(
        deploymentProperties: KtorDeploymentProperties,
        sslProperties: KtorSslProperties?,
        applicationConfig: ApplicationConfig,
        configurers: Set<WebServerConfigurer>
    ): ApplicationEngineEnvironment =
        applicationEngineEnvironment {
            log = logger
            config = applicationConfig

            @OptIn(DelicateCoroutinesApi::class)
            parentCoroutineContext = GlobalScope.coroutineContext

            this.rootPath = deploymentProperties.rootPath
            connector {
                host = deploymentProperties.host
                port = deploymentProperties.port
            }

            sslProperties?.let { props ->
                val keyStore = KeyStore.getInstance("JKS").apply {
                    FileInputStream(props.keyStore).use { load(it, props.keyStorePassword.value.toCharArray()) }
                    requireNotNull(getKey(props.keyAlias, props.privateKeyPassword.value.toCharArray())) {
                        "The specified key ${props.keyAlias} doesn't exist in the key store ${props.keyStore}"
                    }
                }
                sslConnector(
                    keyStore = keyStore,
                    keyAlias = props.keyAlias,
                    keyStorePassword = { props.keyStorePassword.value.toCharArray() },
                    privateKeyPassword = { props.privateKeyPassword.value.toCharArray() }
                ) {
                    host = deploymentProperties.host
                    port = props.sslPort
                    keyStorePath = props.keyStore
                }
            }

            module { configurers.ordered().forEach { it.run { configure() } } }
        }

    @ProvidesIntoSet
    @Singleton
    fun provideApplicationEngineService(
        applicationEngine: ApplicationEngine
    ): Service =
        BlockingService {
            logger.info("Starting Ktor server...")
            applicationEngine.start(wait = true)
            logger.info("Ktor server started")

            applicationEngine.environment.connectors.forEach { connector ->
                when (connector.type) {
                    ConnectorType.HTTP -> URLProtocol.HTTP
                    ConnectorType.HTTPS -> URLProtocol.HTTPS
                    else -> null
                }
                    ?.let { protocol -> URLBuilder(protocol, connector.host, connector.port) }
                    ?.appendPathSegments(applicationEngine.environment.rootPath)
                    ?.buildString()
                    ?.let { url -> logger.info("Ktor is listening on $url") }
            }

            BlockingService.Disposable {
                logger.info("Terminating Ktor server gracefully...")
                applicationEngine.stop()
                logger.info("Ktor server terminated")
            }
        }
}

data class KtorDeploymentProperties(
    val host: String,
    val rootPath: String,
    val port: Int
)

data class KtorSslProperties(
    val sslPort: Int,
    val keyStore: File,
    val keyAlias: String,
    val keyStorePassword: Secret,
    val privateKeyPassword: Secret
)

fun interface WebServerConfigurer {
    fun Application.configure()
}