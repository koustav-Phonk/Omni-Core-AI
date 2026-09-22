package com.example.model

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: AiProviderType = AiProviderType.GEMINI,
    val modelId: String = "gemini-3.5-flash",
    val tokensCount: Int = 0,
    val latencyMs: Long = 0,
    val thinkingContent: String? = null,
    val isError: Boolean = false,
    val imageBase64: String? = null,
    val isStreaming: Boolean = false
)

data class ComparisonTileState(
    val modelId: String,
    val provider: AiProviderType,
    val responseText: String = "",
    val thinkingText: String = "",
    val isGenerating: Boolean = false,
    val latencyMs: Long = 0,
    val error: String? = null
)
