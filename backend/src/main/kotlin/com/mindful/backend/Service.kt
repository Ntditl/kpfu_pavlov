package com.mindful.backend

import com.mindful.core.InterventionDecision
import com.mindful.core.InterventionMode
import com.mindful.core.LaunchAttempt
import com.mindful.core.MindfulSession
import com.mindful.core.MindfulnessEngine
import com.mindful.core.ProgressSnapshot
import com.mindful.core.SessionOutcome
import com.mindful.core.TriggerRule

interface MindfulService {
    fun upsertRule(userId: String, request: RuleRequest)
    fun addSession(userId: String, request: SessionRequest)
    fun decide(userId: String, request: DecisionRequest): InterventionDecision
    fun progress(userId: String): ProgressSnapshot
}

class InMemoryMindfulService : MindfulService {
    private val rules = mutableMapOf<String, MutableMap<String, TriggerRule>>()
    private val sessions = mutableMapOf<String, MutableList<MindfulSession>>()
    private val engine = MindfulnessEngine(
        whitelist = setOf(
            "com.android.dialer",
            "com.android.settings",
            "com.google.android.deskclock"
        )
    )

    override fun upsertRule(userId: String, request: RuleRequest) {
        require(request.packageName.isNotBlank()) { "packageName must not be blank" }
        require(request.bypassLimitPerDay >= 0) { "bypassLimitPerDay must be >= 0" }

        val userRules = rules.getOrPut(userId) { mutableMapOf() }
        userRules[request.packageName] = TriggerRule(
            packageName = request.packageName,
            mode = request.mode.toDomain(),
            bypassLimitPerDay = request.bypassLimitPerDay,
        )
    }

    override fun addSession(userId: String, request: SessionRequest) {
        require(request.packageName.isNotBlank()) { "packageName must not be blank" }

        val userSessions = sessions.getOrPut(userId) { mutableListOf() }
        userSessions += MindfulSession(
            packageName = request.packageName,
            mode = request.mode.toDomain(),
            outcome = request.outcome.toDomain(),
        )
    }

    override fun decide(userId: String, request: DecisionRequest): InterventionDecision {
        require(request.packageName.isNotBlank()) { "packageName must not be blank" }

        val userRules = rules[userId].orEmpty()
        val userSessions = sessions[userId].orEmpty()
        val rule = userRules[request.packageName]

        return engine.decideIntervention(
            attempt = LaunchAttempt(
                packageName = request.packageName,
                unlockedRecently = request.unlockedRecently,
            ),
            rule = rule,
            recentSessions = userSessions,
        )
    }

    override fun progress(userId: String): ProgressSnapshot {
        val userSessions = sessions[userId].orEmpty()
        return engine.computeProgress(userSessions)
    }
}

private fun ApiInterventionMode.toDomain(): InterventionMode = when (this) {
    ApiInterventionMode.NONE -> InterventionMode.NONE
    ApiInterventionMode.SOFT -> InterventionMode.SOFT
    ApiInterventionMode.STRICT -> InterventionMode.STRICT
    ApiInterventionMode.SCHEDULED -> InterventionMode.SCHEDULED
}

private fun ApiSessionOutcome.toDomain(): SessionOutcome = when (this) {
    ApiSessionOutcome.PASSED -> SessionOutcome.PASSED
    ApiSessionOutcome.CANCELLED -> SessionOutcome.CANCELLED
    ApiSessionOutcome.TIMEOUT -> SessionOutcome.TIMEOUT
    ApiSessionOutcome.BYPASS -> SessionOutcome.BYPASS
    ApiSessionOutcome.WHITELISTED -> SessionOutcome.WHITELISTED
}

fun InterventionMode.toApi(): ApiInterventionMode = when (this) {
    InterventionMode.NONE -> ApiInterventionMode.NONE
    InterventionMode.SOFT -> ApiInterventionMode.SOFT
    InterventionMode.STRICT -> ApiInterventionMode.STRICT
    InterventionMode.SCHEDULED -> ApiInterventionMode.SCHEDULED
}
