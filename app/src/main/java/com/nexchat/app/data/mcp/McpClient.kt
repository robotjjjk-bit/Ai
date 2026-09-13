package com.nexchat.app.data.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class McpServer(val id: String, val name: String, val url: String, val headers: Map<String, String> = emptyMap())
data class McpTool(val serverId: String, val name: String, val description: String, val schema: String)

@Singleton
class McpClient @Inject constructor(private val ok: OkHttpClient) {
    suspend fun listTools(s: McpServer): List<McpTool> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("jsonrpc", "2.0").put("id", 1).put("method", "tools/list").put("params", JSONObject())
                .toString().toRequestBody("application/json".toMediaType())
            val b = Request.Builder().url(s.url).post(body)
            s.headers.forEach { (k, v) -> b.header(k, v) }
            val txt = ok.newCall(b.build()).execute().use { it.body?.string() ?: "{}" }
            val arr = JSONObject(txt).optJSONObject("result")?.optJSONArray("tools") ?: JSONArray()
            List(arr.length()) { i ->
                val t = arr.getJSONObject(i)
                McpTool(s.id, t.optString("name"), t.optString("description", ""), t.optJSONObject("inputSchema")?.toString() ?: "{}")
            }
        } catch (_: Exception) { emptyList() }
    }
    suspend fun callTool(s: McpServer, tool: String, args: Map<String, Any?>): String = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("jsonrpc", "2.0").put("id", 2).put("method", "tools/call")
                .put("params", JSONObject().put("name", tool).put("arguments", JSONObject(args as Map<String, *>)))
                .toString().toRequestBody("application/json".toMediaType())
            val b = Request.Builder().url(s.url).post(body)
            s.headers.forEach { (k, v) -> b.header(k, v) }
            ok.newCall(b.build()).execute().use { it.body?.string()?.take(8000) ?: "" }
        } catch (e: Exception) { "mcp-error: ${e.message}" }
    }
}
