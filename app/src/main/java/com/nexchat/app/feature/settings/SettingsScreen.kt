package com.nexchat.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.nexchat.app.core.QrCodec
import com.nexchat.app.data.local.NexDao
import com.nexchat.app.data.model.ProviderEntity
import com.nexchat.app.data.model.ProviderKind
import com.nexchat.app.data.prefs.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVm @Inject constructor(val dao: NexDao, val settings: SettingsStore) : ViewModel() {
    private val _list = MutableStateFlow<List<ProviderEntity>>(emptyList()); val list = _list.asStateFlow()
    suspend fun load() { _list.value = dao.providers() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsVm = hiltViewModel()) {
    val list by vm.list.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { vm.load() }
    Scaffold(topBar = { TopAppBar(title = { Text("Providers & Settings") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
        actions = { IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list) { pr ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(pr.name, style = MaterialTheme.typography.titleMedium)
                        Text("${pr.kind} · ${pr.baseUrl}", style = MaterialTheme.typography.bodySmall)
                        Text("models: ${pr.models}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { scope.launch { vm.dao.deleteProvider(pr.id); vm.load() } }) { Text("Delete") }
                            TextButton(onClick = { /* QR export: encode via QrCodec.providerToJson */ }) { Text("QR Export") }
                        }
                    }
                }
            }
            item {
                ElevatedCard {
                    Column(Modifier.padding(12.dp)) {
                        Text("Search grounding (Brave/Tavily API key)")
                        Text("QR import: gunakan scanner untuk JSON provider {v,name,baseUrl,kind,models,headers}.", style = MaterialTheme.typography.bodySmall)
                        Text("Dark mode: Auto/System + toggle di MainActivity via DataStore. Dynamic color aktif.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    if (showAdd) {
        var name by remember { mutableStateOf("My Provider") }
        var url by remember { mutableStateOf("https://api.openai.com") }
        var models by remember { mutableStateOf("gpt-4o-mini") }
        var key by remember { mutableStateOf("") }
        var headers by remember { mutableStateOf("{}") }
        AlertDialog(onDismissRequest = { showAdd = false }, confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    val id = name.lowercase().replace(" ", "-") + "-" + System.currentTimeMillis()
                    vm.dao.upsertProvider(ProviderEntity(id, name, ProviderKind.CUSTOM, url, models = models, headersJson = headers))
                    vm.settings.putApiKey(id, key); vm.load(); showAdd = false
                }
            }) { Text("Save") }
        }, title = { Text("Add provider") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(url, { url = it }, label = { Text("Base URL") })
                OutlinedTextField(models, { models = it }, label = { Text("Models (comma)") })
                OutlinedTextField(key, { key = it }, label = { Text("API key") })
                OutlinedTextField(headers, { headers = it }, label = { Text("Custom headers JSON") })
            }
        })
    }
}
