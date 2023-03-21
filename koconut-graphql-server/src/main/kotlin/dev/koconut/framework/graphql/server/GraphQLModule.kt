package dev.koconut.framework.graphql.server

import com.apollographql.federation.graphqljava.Federation
import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import dev.koconut.framework.core.config.ConfigBeans
import dev.koconut.framework.core.config.configBean
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*

@AutoService(Module::class)
class GraphQLModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideGraphQLProperties(configBeans: ConfigBeans): GraphQLServerProperties =
        configBeans.configBean("graphql.server")

    @Provides
    @Singleton
    fun provideTypeDefinitionRegistry(properties: GraphQLServerProperties): TypeDefinitionRegistry =
        properties.schemaResources
            .flatMap {
                ClassLoader
                    .getSystemResources(it)
                    .takeIf(Enumeration<URL>::hasMoreElements)
                    ?.toList()
                    ?: throw IOException("Missing schema resource: $it")
            }
            .fold(TypeDefinitionRegistry()) { registry, url ->
                registry.merge(SchemaParser().parse(File(url.file)))
            }

    @Provides
    @Singleton
    fun provideRuntimeWiring(): RuntimeWiring =
        RuntimeWiring.MOCKED_WIRING

    @Provides
    @Singleton
    fun provideGraphQLSchema(
        properties: GraphQLServerProperties,
        typeDefinitionRegistry: TypeDefinitionRegistry,
        runtimeWiring: RuntimeWiring
    ): GraphQLSchema =
        if (properties.federation.enabled) {
            Federation
                .transform(typeDefinitionRegistry, runtimeWiring)
                .build()
        } else {
            SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        }

    @Provides
    @Singleton
    fun provideGraphQL(schema: GraphQLSchema): GraphQL =
        GraphQL
            .newGraphQL(schema)
            .build()
}

data class GraphQLServerProperties(
    val schemaResources: List<String>,
    val federation: GraphQLFederationProperties
)

data class GraphQLFederationProperties(
    val enabled: Boolean
)