package com.mindful.backend

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::mindfulModule)
        .start(wait = true)
}

fun Application.mindfulModule() {
    configurePlugins()
    configureSerialization()
    configureRouting(InMemoryMindfulService())
}
