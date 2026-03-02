package com.mindful.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindful.core.SessionOutcome

@Composable
fun MindfulApp(viewModel: MindfulViewModel) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Digital Mindfulness Client")
            Spacer(Modifier.height(12.dp))

            Button(onClick = { viewModel.onForegroundPackageDetected("com.social.app", unlockedRecently = true) }) {
                Text("Simulate trigger app launch")
            }

            Spacer(Modifier.height(8.dp))
            Text("Current target: ${state.targetPackage ?: "none"}")
            Text("Reason: ${state.reason}")
            Text(state.progressText)

            if (state.showBarrier) {
                Spacer(Modifier.height(16.dp))
                Text("Intent challenge: Are you opening this app intentionally?")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.onBarrierResult(SessionOutcome.CANCELLED) }) {
                    Text("Cancel launch")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.onBarrierResult(SessionOutcome.PASSED) }) {
                    Text("Continue mindfully")
                }
            }
        }
    }
}
