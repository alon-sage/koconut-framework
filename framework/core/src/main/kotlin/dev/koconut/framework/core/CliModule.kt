package dev.koconut.framework.core

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.Stage
import com.google.inject.multibindings.Multibinder
import com.google.inject.multibindings.ProvidesIntoSet

@AutoService(Module::class)
class CliModule : AbstractModule() {
    override fun configure() {
        Multibinder.newSetBinder(binder(), CliktCommand::class.java)
    }

    @Provides
    @Singleton
    fun provideMainCommand(commands: Set<CliktCommand>): CliktCommand =
        NoOpCliktCommand(name = "main", printHelpOnEmptyArgs = true)
            .subcommands(commands)

    @ProvidesIntoSet
    @Singleton
    fun provideCompletionCommand(): CliktCommand =
        CompletionCommand(name = "completion")
}

fun runApplication(
    args: Array<String>,
    configurer: InjectorConfiguration.() -> Unit = {}
) {
    applicationInjector {
        stage(Stage.PRODUCTION)
        configurer()
    }
        .getInstance<CliktCommand>()
        .main(args)
}