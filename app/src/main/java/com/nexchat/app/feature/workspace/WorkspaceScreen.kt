package com.nexchat.app.feature.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkVm @Inject constructor(val mgr: WorkspaceManager) : ViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(onBack: () -> Unit, vm: WorkVm = hiltViewModel()) {
    var files by remember { mutableStateOf(vm.mgr.list()) }
    var name by remember { mutableStateOf("notes.md") }
    var body by remember { mutableStateOf("# Workspace\nAgent dapat membaca folder ini sebagai konteks.") }
    Scaffold(topBar = { TopAppBar(title = { Text("Agent Workspace") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Column(Modifier.padding(p).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("File") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(body, { body = it }, Modifier.fillMaxWidth().height(160.dp), label = { Text("Content") })
            Button(onClick = { vm.mgr.write(name, body); files = vm.mgr.list() }) { Text("Save to workspace") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(files) { f -> ListItem(headlineContent = { Text(f.name) }, supportingContent = { Text("${f.length()} bytes") }) }
            }
            Text("Chat otomatis menyertakan ringkasan workspace sebagai konteks. MCP tools dapat dipanggil dari agent loop (lihat McpClient).", style = MaterialTheme.typography.bodySmall)
        }
    }
}
