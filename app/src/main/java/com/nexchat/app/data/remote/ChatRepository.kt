package com.nexchat.app.data.remote

import com.nexchat.app.data.model.ProviderKind
import com.nexchat.app.data.prefs.SettingsStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val ok: OkHttpClient,
    private val settings: SettingsStore,
) {
    fun endpointFor(kind: ProviderKind, base: String): String = when (kind) {
        ProviderKind.ANTHROPIC -> "$base/v1/messages"
        ProviderKind.GEMINI -> "$base/v1beta/models"
        else -> "${base.trimEnd('/')}/v1/chat/completions"
    }

    fun streamChat(
        kind: ProviderKind, baseUrl: String, apiKey: String, model: String,
        sys: String, history: List<Pair<String, String>>,
        extraHeaders: Map<String, String> = emptyMap(),
        searchContext: String = "",
    ): Flow<String> = callbackFlow {
        val body = buildBody(kind, model, sys, history, searchContext)
        val reqB = Request.Builder().url(endpointFor(kind, baseUrl.ifBlank { "https://api.openai.com" }))
            .post(RequestBody.create("application/json".toMediaType(), body.toString()))
        if (apiKey.isNotBlank()) {
            if (kind == ProviderKind.ANTHROPIC) { reqB.header("x-api-key", apiKey); reqB.header("anthropic-version", "2023-06-01") }
            else reqB.header("Authorization", "Bearer $apiKey")
        }
        extraHeaders.forEach { (k, v) -> reqB.header(k, v) }
        val factory = EventSources.createFactory(ok)
        val listener = object : okhttp3.sse.EventSourceListener() {
            override fun onEvent(es: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") { es.cancel(); close(); return }
                trySend(extractDelta(kind, data))
            }
            override fun onFailure(es: EventSource, t: Throwable?, r: Response?) { close(t) }
            override fun onClosed(es: EventSource) { close() }
        }
        val es = factory.newEventSource(reqB.build(), listener)
        awaitClose { es.cancel() }
    }

    private fun buildBody(kind: ProviderKind, model: String, sys: String, h: List<Pair<String, String>>, search: String): JSONObject {
        val o = JSONObject()
        if (kind == ProviderKind.ANTHROPIC) {
            o.put("model", model); o.put("max_tokens", 4096); o.put("stream", true)
            if (sys.isNotBlank()) o.put("system", sys + "\n" + search)
            val arr = JSONArray()
            h.forEach { (r, c) -> arr.put(JSONObject().put("role", r).put("content", c)) }
            o.put("messages", arr)
        } else {
            o.put("model", model); o.put("stream", true); o.put("temperature", 0.7)
            val arr = JSONArray()
            if (sys.isNotBlank() || search.isNotBlank()) arr.put(JSONObject().put("role", "system").put("content", "$sys\n$search"))
            h.forEach { (r, c) ->
                if (c.startsWith("[IMG]")) {
                    val b64 = c.removePrefix("[IMG]")
                    arr.put(JSONObject().put("role", r).put("content", JSONArray()
                        .put(JSONObject().put("type", "text").put("text", "Describe this image"))
                        .put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$b64")))))
                } else arr.put(JSONObject().put("role", r).put("content", c))
            }
            o.put("messages", arr)
        }
        return o
    }

    private fun extractDelta(kind: ProviderKind, data: String): String = try {
        val j = JSONObject(data)
        if (kind == ProviderKind.ANTHROPIC) j.optJSONObject("delta")?.optString("text", "") ?: ""
        else j.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("delta")?.optString("content", "") ?: ""
    } catch (_: Exception) { "" }
}
