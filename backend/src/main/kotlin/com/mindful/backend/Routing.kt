package com.mindful.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(service: MindfulService) {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        route("/api/v1/users/{userId}") {
            post("/rules") {
                val userId = call.parameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("userId is required"))
                val request = call.receive<RuleRequest>()
                withValidation {
                    service.upsertRule(userId, request)
                }
                call.respond(HttpStatusCode.Created)
            }

            post("/sessions") {
                val userId = call.parameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("userId is required"))
                val request = call.receive<SessionRequest>()
                withValidation {
                    service.addSession(userId, request)
                }
                call.respond(HttpStatusCode.Created)
            }

            post("/decide") {
                val userId = call.parameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("userId is required"))
                val request = call.receive<DecisionRequest>()
                val decision = withValidation {
                    service.decide(userId, request)
                }
                call.respond(
                    DecisionResponse(
                        shouldIntervene = decision.shouldIntervene,
                        effectiveMode = decision.effectiveMode.toApi(),
                        reason = decision.reason,
                    )
                )
            }

            get("/progress") {
                val userId = call.parameters["userId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("userId is required"))
                val progress = service.progress(userId)
                call.respond(
                    ProgressResponse(
                        disciplinePoints = progress.disciplinePoints,
                        streak = progress.streak,
                        mindfulLaunchRate = progress.mindfulLaunchRate,
                        impulseCancelRate = progress.impulseCancelRate,
                    )
                )
            }
        }
    }
}

private inline fun <T> withValidation(block: () -> T): T {
    return try {
        block()
    } catch (e: IllegalArgumentException) {
        throw ValidationException(e.message ?: "validation failed")
    }
}

class ValidationException(message: String) : RuntimeException(message)
