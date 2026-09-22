package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "gemini",
    val modelId: String = "gemini-3.5-flash",
    val tokensCount: Int = 0,
    val latencyMs: Long = 0,
    val thinkingContent: String? = null,
    val isError: Boolean = false,
    val imageBase64: String? = null
)
