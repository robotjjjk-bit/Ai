package com.nexchat.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProviderKind { OPENAI, ANTHROPIC, GEMINI, OLLAMA, CUSTOM }

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: ProviderKind,
    val baseUrl: String,
    val apiKeyRef: String = "",
    val models: String = "",
    val headersJson: String = "{}",
    val enabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val providerId: String,
    val model: String,
    val systemPrompt: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val memorySummary: String = "",
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val parentId: String? = null,
    val branchIndex: Int = 0,
    val attachmentsJson: String = "[]",
    val tokens: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class Attachment(
    val id: String,
    val kind: String,
    val name: String,
    val mime: String,
    val uri: String = "",
    val textExcerpt: String = "",
    val base64: String = "",
)

data class ChatRequest(
    val providerId: String,
    val model: String,
    val messages: List<Map<String, Any>>,
    val stream: Boolean = true,
    val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
)
