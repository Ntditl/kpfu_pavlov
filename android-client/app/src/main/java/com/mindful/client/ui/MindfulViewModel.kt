package com.mindful.client.ui

import androidx.lifecycle.ViewModel
import com.mindful.core.InterventionMode
import com.mindful.core.LaunchAttempt
import com.mindful.core.MindfulSession
import com.mindful.core.MindfulnessEngine
import com.mindful.core.SessionOutcome
import com.mindful.core.TriggerRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BarrierUiState(
    val targetPackage: String? = null,
    val mode: InterventionMode = InterventionMode.NONE,
    val reason: String = "",
    val showBarrier: Boolean = false,
    val progressText: String = "",
)

class MindfulViewModel : ViewModel() {
    private val engine = MindfulnessEngine(
        whitelist = setOf(
            "com.android.dialer",
            "com.android.settings",
            "com.google.android.deskclock"
        )
    )

    private val rules = mutableMapOf(
        "com.social.app" to TriggerRule("com.social.app", InterventionMode.SOFT),
        "com.video.app" to TriggerRule("com.video.app", InterventionMode.STRICT)
    )

    private val sessions = mutableListOf<MindfulSession>()

    private val _uiState = MutableStateFlow(BarrierUiState())
    val uiState: StateFlow<BarrierUiState> = _uiState.asStateFlow()

    fun onForegroundPackageDetected(packageName: String, unlockedRecently: Boolean) {
        val rule = rules[packageName]
        val decision = engine.decideIntervention(
            attempt = LaunchAttempt(packageName = packageName, unlockedRecently = unlockedRecently),
            rule = rule,
            recentSessions = sessions,
        )

        _uiState.value = _uiState.value.copy(
            targetPackage = packageName,
            mode = decision.effectiveMode,
            reason = decision.reason,
            showBarrier = decision.shouldIntervene,
        )
    }

    fun onBarrierResult(outcome: SessionOutcome) {
        val state = _uiState.value
        val pkg = state.targetPackage ?: return

        sessions += MindfulSession(pkg, state.mode, outcome)
        val progress = engine.computeProgress(sessions)

        _uiState.value = state.copy(
            showBarrier = false,
            progressText = "Points: ${progress.disciplinePoints}, Streak: ${progress.streak}, Mindful rate: ${"%.2f".format(progress.mindfulLaunchRate)}"
        )
    }
}
