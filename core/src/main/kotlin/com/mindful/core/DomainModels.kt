package com.mindful.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class InterventionMode {
    NONE,
    SOFT,
    STRICT,
    SCHEDULED
}

enum class SessionOutcome {
    PASSED,
    CANCELLED,
    TIMEOUT,
    BYPASS,
    WHITELISTED
}

data class TriggerRule(
    val packageName: String,
    val mode: InterventionMode,
    val bypassLimitPerDay: Int = 0,
)

data class LaunchAttempt(
    val packageName: String,
    val timestamp: Instant = Clock.System.now(),
    val unlockedRecently: Boolean = false,
)

data class InterventionDecision(
    val shouldIntervene: Boolean,
    val effectiveMode: InterventionMode,
    val reason: String,
)

data class MindfulSession(
    val packageName: String,
    val mode: InterventionMode,
    val outcome: SessionOutcome,
    val timestamp: Instant = Clock.System.now(),
)

data class ProgressSnapshot(
    val disciplinePoints: Int,
    val streak: Int,
    val mindfulLaunchRate: Double,
    val impulseCancelRate: Double,
)
