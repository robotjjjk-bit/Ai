package com.nexchat.app.feature.chat

import android.net.Uri
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexchat.app.core.FileTextExtractor
import com.nexchat.app.core.PromptVariables
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onSettings: () -> Unit, onWork: () -> Unit, vm: ChatViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()
    val provs by vm.providers.collectAsState()
    var input by remember { mutableStateOf("") }
    var showModels by remember { mutableStateOf(false) }
    var varAsk by remember { mutableStateOf<List<String>?>(null) }
    val scope = rememberCoroutineScope()
    val conv = ui.conv

    LaunchedEffect(provs) { if (ui.conv == null && provs.isNotEmpty()) vm.newConversation(provs[0].id, provs[0].models.split(",").firstOrNull() ?: "gpt-4o-mini") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(conv?.title ?: "NexChat") },
                actions = {
                    IconButton(onClick = { showModels = true }) { Icon(Icons.Default.List, null) }
                    IconButton(onClick = onWork) { Icon(Icons.Default.Folder, null) }
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, null) }
                })
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Message, use {{var}}…") }, maxLines = 5)
                FilledTonalButton(onClick = {
                    val vars = PromptVariables.extract(input)
                    if (vars.isNotEmpty()) varAsk = vars
                    else { vm.send(input); input = "" }
                }, enabled = !ui.busy) { Text("Send") }
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ui.messages) { m ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(m.role.uppercase(), style = MaterialTheme.typography.labelSmall)
                        if (m.role == "assistant") MarkdownView(m.content) else Text(m.content)
                        if (m.role == "assistant") TextButton(onClick = { vm.retryAsBranch(m) }) { Text("Branch") }
                    }
                }
            }
            if (ui.streaming.isNotBlank()) item { ElevatedCard { MarkdownView(ui.streaming) } }
        }
    }

    if (showModels && conv != null) ModalBottomSheet(onDismissRequest = { showModels = false }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Model picker", style = MaterialTheme.typography.titleLarge)
            provs.forEach { p ->
                Text(p.name, style = MaterialTheme.typography.labelSmall)
                p.models.split(",").filter { it.isNotBlank() }.forEach { mo ->
                    FilterChip(mo.trim() == conv.model, { vm.newConversation(p.id, mo.trim()); showModels = false }, label = { Text(mo.trim()) })
                }
            }
        }
    }
    varAsk?.let { vars ->
        var vals by remember { mutableStateOf(vars.associateWith { "" }) }
        AlertDialog(onDismissRequest = { varAsk = null }, confirmButton = {
            TextButton(onClick = { vm.send(input, vals); input = ""; varAsk = null }) { Text("Send") }
        }, title = { Text("Fill variables") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vals.forEach { (k, v) -> OutlinedTextField(v, { vals = vals + (k to it) }, label = { Text(k) }) }
            }
        })
    }
}

@Composable
fun MarkdownView(md: String) {
    val html = remember(md) { MarkdownHtml.wrap(md) }
    AndroidView(factory = { c -> WebView(c).apply { settings.javaScriptEnabled = true; loadWithOverviewMode = true } },
        update = { it.loadDataWithBaseURL(null, html, "text/html", "utf-8", null) }, modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp))
}

object MarkdownHtml {
    fun wrap(md: String): String {
        val esc = md.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        return """<html><head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.css">
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
<style>body{font-family:system-ui;background:transparent;color:inherit}pre{background:#111;color:#eee;padding:12px;border-radius:8px;overflow:auto}table{border-collapse:collapse;width:100%}td,th{border:1px solid #ccc;padding:6px}</style>
</head><body><div id="c"></div>
<script>mermaid.initialize({startOnLoad:false});
document.getElementById('c').innerHTML=marked.parse(document.currentScript.dataset.md||${org.json.JSONObject.quote(md)});
document.querySelectorAll('pre code.language-mermaid').forEach(async el=>{const d=document.createElement('div');d.className='mermaid';d.textContent=el.textContent;el.parentElement.replaceWith(d)});mermaid.run();
</script><pre style="white-space:pre-wrap">${esc}</pre></body></html>"""
    }
}
