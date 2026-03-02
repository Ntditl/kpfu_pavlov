package com.mindful.backend

import kotlinx.serialization.Serializable

@Serializable
enum class ApiInterventionMode {
    NONE,
    SOFT,
    STRICT,
    SCHEDULED,
}

@Serializable
enum class ApiSessionOutcome {
    PASSED,
    CANCELLED,
    TIMEOUT,
    BYPASS,
    WHITELISTED,
}

@Serializable
data class RuleRequest(
    val packageName: String,
    val mode: ApiInterventionMode,
    val bypassLimitPerDay: Int = 0,
)

@Serializable
data class SessionRequest(
    val packageName: String,
    val mode: ApiInterventionMode,
    val outcome: ApiSessionOutcome,
)

@Serializable
data class DecisionRequest(
    val packageName: String,
    val unlockedRecently: Boolean = false,
)

@Serializable
data class DecisionResponse(
    val shouldIntervene: Boolean,
    val effectiveMode: ApiInterventionMode,
    val reason: String,
)

@Serializable
data class ProgressResponse(
    val disciplinePoints: Int,
    val streak: Int,
    val mindfulLaunchRate: Double,
    val impulseCancelRate: Double,
)

@Serializable
data class ErrorResponse(
    val message: String,
)
