import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

plugins {
    kotlin("jvm")
    id("dev.koconut.conventions")
    id("com.google.protobuf")
}

dependencies {
    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")
    api("com.google.protobuf:protobuf-kotlin")
}

val koconutBomSpec = loadPropertyFromResources("bom-spec.properties", "koconut-bom")

val protocId = DefaultModuleIdentifier.newId("com.google.protobuf", "protoc")
val protocGrpcId = DefaultModuleIdentifier.newId("io.grpc", "protoc-gen-grpc-java")
val protocGrpcKtId = DefaultModuleIdentifier.newId("io.grpc", "protoc-gen-grpc-kotlin")

val protobufArtifacts = configurations
    .detachedConfiguration(
        dependencies.platform(koconutBomSpec),
        dependencies.create(protocId),
        dependencies.create(protocGrpcId),
        dependencies.create(protocGrpcKtId),
    )
    .resolvedConfiguration
    .firstLevelModuleDependencies

protobuf {
    protoc {
        artifact = protobufArtifacts.single { it.module.id.module == protocId }.module.id.toString()
    }
    plugins {
        create("grpc") {
            artifact = protobufArtifacts.single { it.module.id.module == protocGrpcId }.module.id.toString()
        }
        create("grpckt") {
            artifact = protobufArtifacts.single { it.module.id.module == protocGrpcKtId }.module.id.toString()
        }
    }
    generateProtoTasks.all().configureEach {
        plugins {
            create("grpc")
            create("grpckt")
        }
        builtins {
            create("kotlin")
        }
    }
}
