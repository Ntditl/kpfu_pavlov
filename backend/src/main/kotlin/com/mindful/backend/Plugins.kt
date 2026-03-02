package com.mindful.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

fun Application.configurePlugins() {
    install(StatusPages, ::statusPages)
}

private fun statusPages(config: StatusPagesConfig) {
    config.exception<ValidationException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "validation failed"))
    }
}
