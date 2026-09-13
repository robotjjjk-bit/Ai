package com.nexchat.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexchat.app.core.MemoryManager
import com.nexchat.app.core.PromptVariables
import com.nexchat.app.data.local.NexDao
import com.nexchat.app.data.model.*
import com.nexchat.app.data.prefs.SettingsStore
import com.nexchat.app.data.remote.ChatRepository
import com.nexchat.app.feature.workspace.WorkspaceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class ChatUi(val conv: ConversationEntity? = null, val messages: List<MessageEntity> = emptyList, val streaming: String = "", val busy: Boolean = false)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dao: NexDao,
    private val repo: ChatRepository,
    private val settings: SettingsStore,
    private val work: WorkspaceManager,
) : ViewModel() {
    private val _ui = MutableStateFlow(ChatUi()); val ui: StateFlow<ChatUi> = _ui.asStateFlow()
    private val _providers = MutableStateFlow<List<ProviderEntity>>(emptyList()); val providers = _providers.asStateFlow()
    private var job: Job? = null

    init { viewModelScope.launch { _providers.value = dao.providers().ifEmpty { defaults() } } }

    private suspend fun defaults(): List<ProviderEntity> {
        val d = listOf(
            ProviderEntity("openai", "OpenAI", ProviderKind.OPENAI, "https://api.openai.com", models = "gpt-4o-mini,gpt-4o"),
            ProviderEntity("ollama", "Ollama Local", ProviderKind.OLLAMA, "http://10.0.2.2:11434", models = "llama3.1,qwen2.5"),
        )
        d.forEach { dao.upsertProvider(it) }
        return d
    }

    fun newConversation(providerId: String, model: String) {
        _ui.value = ChatUi(ConversationEntity(PromptVariables.newId(), "New chat", providerId, model))
    }

    fun send(raw: String, varValues: Map<String, String> = emptyMap(), useSearch: Boolean = false, searchKey: String = "", searchKind: String = "brave") {
        val conv = _ui.value.conv ?: return
        val text = PromptVariables.render(raw, varValues)
        viewModelScope.launch {
            val full = text + "\n\n" + work.contextBlock()
            val uMsg = MessageEntity(PromptVariables.newId(), conv.id, "user", full)
            dao.upsertMessage(uMsg)
            val prov = _providers.value.firstOrNull { it.id == conv.providerId } ?: return@launch
            val key = settings.getApiKey(prov.id)
            val headers = try { JSONObject(prov.headersJson).keys().asSequence().associateWith { JSONObject(prov.headersJson).getString(it) } } catch (_: Exception) { emptyMap() }
            val hist = dao.messages(conv.id).map { it.role to it.content } + ("user" to full)
            val sys = MemoryManager.buildMemoryPrefix(conv.memorySummary, emptyList()) + "\n" + conv.systemPrompt
            _ui.value = _ui.value.copy(busy = true, streaming = "")
            val acc = StringBuilder()
            job = launch {
                repo.streamChat(prov.kind, prov.baseUrl, key, conv.model, sys, hist, headers).collect { d ->
                    acc.append(d); _ui.value = _ui.value.copy(streaming = acc.toString())
                }
                dao.upsertMessage(MessageEntity(PromptVariables.newId(), conv.id, "assistant", acc.toString(), parentId = uMsg.id))
                dao.upsertConversation(conv.copy(title = conv.title.ifBlank { text.take(40) }))
                _ui.value = _ui.value.copy(messages = dao.messages(conv.id), streaming = "", busy = false)
            }
        }
    }

    fun retryAsBranch(msg: MessageEntity) {
        viewModelScope.launch {
            val sibs = dao.branches(msg.parentId ?: return@launch).size
            val copy = msg.copy(id = PromptVariables.newId(), branchIndex = sibs, content = msg.content + " (branch $sibs — edit & resend to fork)")
            dao.upsertMessage(copy)
            _ui.value = _ui.value.copy(messages = dao.messages(msg.conversationId))
        }
    }

    fun stop() { job?.cancel(); _ui.value = _ui.value.copy(busy = false) }
    suspend fun refreshMessages(id: String) { _ui.value = _ui.value.copy(messages = dao.messages(id)) }
}
