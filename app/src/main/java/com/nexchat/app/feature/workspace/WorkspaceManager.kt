package com.nexchat.app.feature.workspace

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceManager @Inject constructor(@ApplicationContext ctx: Context) {
    val root = File(ctx.filesDir, "workspace").apply { mkdirs() }
    fun list(rel: String = ""): List<File> = File(root, rel).listFiles()?.sortedBy { it.name } ?: emptyList()
    fun read(rel: String): String = File(root, rel).takeIf { it.isFile }?.readText()?.take(20000) ?: ""
    fun write(rel: String, text: String) { val f = File(root, rel); f.parentFile?.mkdirs(); f.writeText(text) }
    fun contextBlock(maxFiles: Int = 8): String {
        val files = root.walkTopDown().filter { it.isFile }.take(maxFiles).toList()
        if (files.isEmpty()) return ""
        return buildString {
            append("Workspace files:\n")
            files.forEach { append("- ${it.relativeTo(root)} (${it.length()}b):\n${it.readText().take(1500)}\n\n") }
        }.take(9000)
    }
}
