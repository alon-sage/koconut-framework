package dev.koconut.framework.core

import com.google.auto.service.AutoService
import com.google.inject.AbstractModule
import com.google.inject.Module
import org.slf4j.bridge.SLF4JBridgeHandler

@AutoService(Module::class)
class LoggingModule : AbstractModule() {
    init {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
    }
}