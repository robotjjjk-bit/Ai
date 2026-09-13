package com.nexchat.app.core

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class FileTextExtractor @Inject constructor(@ApplicationContext private val ctx: Context) {
    suspend fun extract(uri: Uri, mime: String?): String = withContext(Dispatchers.IO) {
        try {
            when {
                mime?.contains("pdf") == true -> "PDF(${uri.lastPathSegment}): ${ctx.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { r -> "pages=${r.pageCount}" }
                }}"
                mime?.contains("officedocument") == true || uri.toString().endsWith(".docx") ->
                    ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().size.let { n -> "DOCX bytes=$n (full parse needs POI on build machine)" } } ?: ""
                mime?.startsWith("text") == true || uri.toString().endsWith(".md") || uri.toString().endsWith(".txt") ->
                    ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()?.take(12000) ?: ""
                mime?.startsWith("image") == true ->
                    ctx.contentResolver.openInputStream(uri)?.use { "[IMG]" + Base64.encodeToString(it.readBytes(), Base64.NO_WRAP).take(900000) } ?: ""
                else -> ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()?.take(8000) ?: ""
            }
        } catch (e: Exception) { "extract-error: ${e.message}" }
    }
}

class SearchClient @Inject constructor(private val ok: OkHttpClient) {
    suspend fun ground(query: String, kind: String, key: String): String = withContext(Dispatchers.IO) {
        if (key.isBlank() || query.isBlank()) return@withContext ""
        try {
            val req = when (kind) {
                "tavily" -> Request.Builder().url("https://api.tavily.com/search")
                    .post(okhttp3.RequestBody.create("application/json".toMediaType(),
                        JSONObject().put("api_key", key).put("query", query).put("max_results", 5).toString()))
                    .build()
                else -> Request.Builder().url("https://api.search.brave.com/res/v1/web/search?q=" + java.net.URLEncoder.encode(query, "UTF-8"))
                    .header("X-Subscription-Token", key).build()
            }
            ok.newCall(req).execute().use { it.body?.string()?.take(6000) ?: "" }
        } catch (_: Exception) { "" }
    }
}
private fun String.toMediaType() = okhttp3.MediaType.parse(this)!!
