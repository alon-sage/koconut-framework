import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

plugins {
    kotlin("jvm")
    id("dev.koconut.conventions")
    id("com.google.protobuf")
}

dependencies {
    api("io.grpc:grpc-protobuf")
    api("io.grpc:grpc-kotlin-stub")
    api("com.google.protobuf:protobuf-kotlin")
}

val koconutBom = dependencies.platform(loadPropertyFromResources("bom-spec.properties", "koconut-bom"))
val protoc = dependencies.create("com.google.protobuf", "protoc")
val protocGrpc = dependencies.create("io.grpc", "protoc-gen-grpc-java")
val protocGrpcKotlin = dependencies.create("io.grpc", "protoc-gen-grpc-kotlin")

val protobufArtifacts: Set<ResolvedDependency> = configurations
    .detachedConfiguration(koconutBom, protoc, protocGrpc, protocGrpcKotlin)
    .resolvedConfiguration
    .firstLevelModuleDependencies

protobuf {
    protoc {
        artifact = protobufArtifacts.single { it.module.id.module == protoc.module }.name
    }
    plugins {
        create("grpc") {
            artifact = protobufArtifacts.single { it.module.id.module == protocGrpc.module }.name
        }
        create("grpcKotlin") {
            artifact = protobufArtifacts.single { it.module.id.module == protocGrpcKotlin.module }.name + ":jdk8@jar"
        }
    }
    generateProtoTasks.all().configureEach {
        plugins {
            create("grpc")
            create("grpcKotlin")
        }
        builtins {
            create("kotlin")
        }
    }
}
