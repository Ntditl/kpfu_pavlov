package com.mindful.backend

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutingTest {
    @Test
    fun `health endpoint returns ok`() = testApplication {
        application { mindfulModule() }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(true, response.bodyAsText().contains("ok"))
    }

    @Test
    fun `session ingestion updates progress`() = testApplication {
        application { mindfulModule() }
        val jsonClient = createClient {
            install(ContentNegotiation) { json() }
        }

        val userId = "u1"

        val created = jsonClient.post("/api/v1/users/$userId/sessions") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                SessionRequest(
                    packageName = "com.social.app",
                    mode = ApiInterventionMode.SOFT,
                    outcome = ApiSessionOutcome.CANCELLED,
                )
            )
        }
        assertEquals(HttpStatusCode.Created, created.status)

        val progressResponse = jsonClient.get("/api/v1/users/$userId/progress")
        assertEquals(HttpStatusCode.OK, progressResponse.status)

        val progress = progressResponse.body<ProgressResponse>()
        assertEquals(5, progress.disciplinePoints)
        assertEquals(1, progress.streak)
        assertEquals(1.0, progress.mindfulLaunchRate)
    }

    @Test
    fun `decide endpoint respects configured rule`() = testApplication {
        application { mindfulModule() }
        val jsonClient = createClient {
            install(ContentNegotiation) { json() }
        }

        val userId = "u2"

        val ruleCreated = jsonClient.post("/api/v1/users/$userId/rules") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                RuleRequest(
                    packageName = "com.social.app",
                    mode = ApiInterventionMode.SOFT,
                    bypassLimitPerDay = 1,
                )
            )
        }
        assertEquals(HttpStatusCode.Created, ruleCreated.status)

        val decisionResponse = jsonClient.post("/api/v1/users/$userId/decide") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(DecisionRequest(packageName = "com.social.app", unlockedRecently = true))
        }

        assertEquals(HttpStatusCode.OK, decisionResponse.status)
        val decision = decisionResponse.body<DecisionResponse>()
        assertEquals(true, decision.shouldIntervene)
        assertEquals(ApiInterventionMode.STRICT, decision.effectiveMode)
        assertEquals("adaptive_escalation", decision.reason)
    }

    @Test
    fun `invalid rule request returns 400`() = testApplication {
        application { mindfulModule() }
        val jsonClient = createClient {
            install(ContentNegotiation) { json() }
        }

        val userId = "u3"

        val response = jsonClient.post("/api/v1/users/$userId/rules") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                RuleRequest(
                    packageName = "",
                    mode = ApiInterventionMode.SOFT,
                    bypassLimitPerDay = -1,
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = response.body<ErrorResponse>()
        assertEquals(true, error.message.isNotBlank())
    }
}
