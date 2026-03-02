package com.mindful.core

class MindfulnessEngine(
    private val whitelist: Set<String>,
) {
    fun decideIntervention(
        attempt: LaunchAttempt,
        rule: TriggerRule?,
        recentSessions: List<MindfulSession>,
    ): InterventionDecision {
        if (attempt.packageName in whitelist) {
            return InterventionDecision(
                shouldIntervene = false,
                effectiveMode = InterventionMode.NONE,
                reason = "whitelisted"
            )
        }

        if (rule == null || rule.mode == InterventionMode.NONE) {
            return InterventionDecision(false, InterventionMode.NONE, "no_rule")
        }

        val frequentRepeats = recentSessions.takeLast(5).count { it.packageName == attempt.packageName } >= 3
        val elevatedMode = when {
            rule.mode == InterventionMode.SOFT && (attempt.unlockedRecently || frequentRepeats) -> InterventionMode.STRICT
            else -> rule.mode
        }

        return InterventionDecision(
            shouldIntervene = elevatedMode != InterventionMode.NONE,
            effectiveMode = elevatedMode,
            reason = if (elevatedMode != rule.mode) "adaptive_escalation" else "rule_applied"
        )
    }

    fun computeProgress(sessions: List<MindfulSession>): ProgressSnapshot {
        if (sessions.isEmpty()) return ProgressSnapshot(0, 0, 0.0, 0.0)

        val mindfulOutcomes = setOf(SessionOutcome.PASSED, SessionOutcome.CANCELLED)
        val mindfulCount = sessions.count { it.outcome in mindfulOutcomes }
        val cancelledCount = sessions.count { it.outcome == SessionOutcome.CANCELLED }
        val nonWhitelistedCount = sessions.count { it.outcome != SessionOutcome.WHITELISTED }

        val points = sessions.fold(0) { acc, session ->
            acc + when (session.outcome) {
                SessionOutcome.CANCELLED -> 5
                SessionOutcome.PASSED -> 2
                SessionOutcome.TIMEOUT -> 0
                SessionOutcome.BYPASS -> -1
                SessionOutcome.WHITELISTED -> 0
            }
        }.coerceAtLeast(0)

        var streak = 0
        for (session in sessions.asReversed()) {
            if (session.outcome == SessionOutcome.CANCELLED || session.outcome == SessionOutcome.PASSED) {
                streak += 1
            } else {
                break
            }
        }

        val mindfulRate = mindfulCount.toDouble() / sessions.size
        val cancelRate = if (nonWhitelistedCount == 0) 0.0 else cancelledCount.toDouble() / nonWhitelistedCount

        return ProgressSnapshot(
            disciplinePoints = points,
            streak = streak,
            mindfulLaunchRate = mindfulRate,
            impulseCancelRate = cancelRate,
        )
    }
}
