package com.mindful.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MindfulnessEngineTest {
    private val engine = MindfulnessEngine(whitelist = setOf("com.android.dialer"))

    @Test
    fun `whitelisted app should not be intercepted`() {
        val decision = engine.decideIntervention(
            attempt = LaunchAttempt("com.android.dialer"),
            rule = TriggerRule("com.android.dialer", InterventionMode.STRICT),
            recentSessions = emptyList(),
        )

        assertFalse(decision.shouldIntervene)
        assertEquals("whitelisted", decision.reason)
    }

    @Test
    fun `soft rule escalates to strict for repeated launches`() {
        val recent = List(5) {
            MindfulSession("com.social.app", InterventionMode.SOFT, SessionOutcome.PASSED)
        }
        val decision = engine.decideIntervention(
            attempt = LaunchAttempt("com.social.app"),
            rule = TriggerRule("com.social.app", InterventionMode.SOFT),
            recentSessions = recent,
        )

        assertTrue(decision.shouldIntervene)
        assertEquals(InterventionMode.STRICT, decision.effectiveMode)
        assertEquals("adaptive_escalation", decision.reason)
    }

    @Test
    fun `progress should prioritize cancellations with higher points`() {
        val sessions = listOf(
            MindfulSession("a", InterventionMode.SOFT, SessionOutcome.CANCELLED),
            MindfulSession("a", InterventionMode.SOFT, SessionOutcome.PASSED),
            MindfulSession("a", InterventionMode.SOFT, SessionOutcome.BYPASS),
            MindfulSession("a", InterventionMode.SOFT, SessionOutcome.WHITELISTED),
        )

        val snapshot = engine.computeProgress(sessions)

        assertEquals(6, snapshot.disciplinePoints)
        assertEquals(0, snapshot.streak)
        assertEquals(1.0 / 3.0, snapshot.impulseCancelRate)
    }
}
