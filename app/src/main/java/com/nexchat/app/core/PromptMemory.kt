package com.nexchat.app.core

import java.util.UUID
import java.util.regex.Pattern

object PromptVariables {
    private val pat = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}")
    fun extract(template: String): List<String> {
        val m = pat.matcher(template); val out = linkedSetOf<String>()
        while (m.find()) out += m.group(1)
        return out.toList()
    }
    fun render(template: String, values: Map<String, String>): String {
        var s = template
        values.forEach { (k, v) -> s = s.replace(Regex("\\{\\{\\s*$k\\s*\\}\\}"), v) }
        return s
    }
    fun newId(): String = UUID.randomUUID().toString()
}

object MemoryManager {
    fun buildMemoryPrefix(summary: String, facts: List<String>): String {
        if (summary.isBlank() && facts.isEmpty()) return ""
        return buildString {
            if (summary.isNotBlank()) append("Conversation summary so far:\n$summary\n\n")
            if (facts.isNotEmpty()) { append("Durable facts:\n"); facts.forEach { append("- $it\n") } }
        }
    }
    fun naiveSummarize(history: List<Pair<String, String>>): String =
        history.takeLast(10).joinToString("\n") { (r, c) -> "$r: ${c.take(220)}" }.take(2000)
}
