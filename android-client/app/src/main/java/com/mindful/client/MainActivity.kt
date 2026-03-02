package com.mindful.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mindful.client.ui.MindfulApp
import com.mindful.client.ui.MindfulViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MindfulViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindfulApp(viewModel)
        }
    }
}
