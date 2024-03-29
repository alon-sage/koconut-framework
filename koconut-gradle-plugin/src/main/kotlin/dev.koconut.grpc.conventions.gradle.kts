import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

plugins {
    id("dev.koconut.conventions")
}

with(dependencies) {
    configurations.all {
        when (name) {
            "api" -> {
                add(name, "io.grpc:grpc-protobuf")
                add(name, "io.grpc:grpc-kotlin-stub")
                add(name, "com.google.protobuf:protobuf-kotlin")
            }
        }
    }
}

val koconutBom = dependencies.platform(loadPropertyFromResources("bom-spec.properties", "koconut-bom"))
val protoc = dependencies.create("com.google.protobuf", "protoc")
val protocGrpc = dependencies.create("io.grpc", "protoc-gen-grpc-java")
val protocGrpcKotlin = dependencies.create("io.grpc", "protoc-gen-grpc-kotlin")

val protobufArtifacts: Set<ResolvedDependency> = configurations
    .detachedConfiguration(koconutBom, protoc, protocGrpc, protocGrpcKotlin)
    .resolvedConfiguration
    .firstLevelModuleDependencies

plugins.withType<ProtobufPlugin> {
    configure<ProtobufExtension> {
        protoc {
            artifact = protobufArtifacts.single { it.module.id.module == protoc.module }.name
        }
        plugins {
            create("grpc") {
                artifact = protobufArtifacts.single { it.module.id.module == protocGrpc.module }.name
            }
            create("grpcKotlin") {
                artifact =
                    protobufArtifacts.single { it.module.id.module == protocGrpcKotlin.module }.name + ":jdk8@jar"
            }
        }
        generateProtoTasks.all().all {
            plugins {
                create("grpc")
                create("grpcKotlin")
            }
            builtins {
                create("kotlin")
            }
        }
    }
}
