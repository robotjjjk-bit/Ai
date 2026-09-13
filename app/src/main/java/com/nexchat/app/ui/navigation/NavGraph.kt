package com.nexchat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexchat.app.feature.chat.ChatScreen
import com.nexchat.app.feature.settings.SettingsScreen
import com.nexchat.app.feature.workspace.WorkspaceScreen

object Routes { const val CHAT = "chat"; const val SETTINGS = "settings"; const val WORK = "work" }

@Composable
fun NexNav() {
    val nav = rememberNavController()
    NavHost(nav, Routes.CHAT) {
        composable(Routes.CHAT) { ChatScreen(onSettings = { nav.navigate(Routes.SETTINGS) }, onWork = { nav.navigate(Routes.WORK) }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.WORK) { WorkspaceScreen(onBack = { nav.popBackStack() }) }
    }
}
