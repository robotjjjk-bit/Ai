package com.nexchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nexchat.app.ui.navigation.NexNav
import com.nexchat.app.ui.theme.NexChatTheme
import com.nexchat.app.data.prefs.SettingsStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settings: SettingsStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark by settings.darkModeFlow.collectAsState(initial = null)
            val dynamic by settings.dynamicColorFlow.collectAsState(initial = true)
            NexChatTheme(darkTheme = dark, dynamicColor = dynamic) {
                NexNav()
            }
        }
    }
}
