# Koconut

Koconut is an application framework that:

* Centered on Kotlin and coroutines;
* Simple but solid like a nut.

The core of the framework is consist of well-known libraries and frameworks:

* [Guice](https://github.com/google/guice) for DI;
* [Typesafe Config](https://github.com/lightbend/config) for configs;
* [Clikt](https://github.com/ajalt/clikt) for CLI;
* [SLF4J](https://github.com/qos-ch/slf4j)
  and [Logback](https://github.com/qos-ch/logback) for logging;
* [Micrometer](https://github.com/micrometer-metrics/micrometer) for metrics;
* [OpenTelemetry](https://github.com/open-telemetry/opentelemetry-java) for
  tracing and another telemetry.

Also, it provides component lifecycle support and basic health check support
(needs manual binding to a web framework).

## Quickstart guide

### Step 1: Set up a new gradle project

Create a project directory and initialize a new Gradle build inside it.

```shell
mkdir koconut-quickstart
cd koconut-quickstart
gradle init --type basic --dsl kotlin
```

### Step 2: Add your code

Open up the project in your IDE and change the `build.gradle.kts` script content
to the next:

```kotlin
plugins {
    id("dev.koconut.conventions") version "0.1.0-alpha3"
    kotlin("jvm")
    kotlin("kapt")
    application
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

dependencies {
    implementation("dev.koconut.framework:koconut-core")
}
```

Now create the `QuickStartApplicationModule.kt` file in
the `src/main/kotlin/com/example` folder and insert the content of
the following snippet into the file.

```kotlin
package com.example

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Singleton
import com.google.inject.multibindings.ProvidesIntoSet
import dev.koconut.framework.core.runApplication
import dev.koconut.framework.core.Service
import kotlinx.coroutines.delay

@AutoService(Module::class)
class QuickStartApplicationModule : AbstractModule() {
    @ProvidesIntoSet
    @Singleton
    fun provideExampleService(): Service =
        Service {
            println("Example service starting...")
            delay(1_000L) // imitate startup
            println("Example service started")

            Service.Disposable {
                println("Example service terminating...")
                delay(1_000L) // imitate termination
                println("Example service terminated")
            }
        }
}

fun main(args: Array<String>) {
    runApplication(args)
}
```

### Step 3: Try it

Let’s build and run the program. Open a command line (or terminal) and navigate
to the folder where you have the project files. We can build and run the
application by issuing the following command:

```shell
./gradlew run --args serve --quite
```

You should see some output that looks very similar to this:

```
Example service starting...
Example service started
```