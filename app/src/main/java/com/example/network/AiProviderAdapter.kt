package com.example.network

import com.example.model.AiModelCapabilities
import com.example.model.AiModelInfo
import com.example.model.AiProviderType

data class ChatMessagePayload(
    val role: String,
    val content: String,
    val imageBase64: String? = null
)

data class ProviderResponse(
    val text: String,
    val thinkingContent: String? = null,
    val tokensCount: Int = 0,
    val latencyMs: Long = 0,
    val error: String? = null
)

data class KeyValidationResult(
    val isValid: Boolean,
    val latencyMs: Long = 0,
    val message: String = "",
    val discoveredModels: List<AiModelInfo> = emptyList()
)

interface AiProviderAdapter {
    val providerType: AiProviderType

    suspend fun sendMessage(
        apiKey: String,
        modelId: String,
        messages: List<ChatMessagePayload>,
        endpoint: String? = null,
        temperature: Float = 0.7f,
        thinkingLevel: String? = null,
        enableWebSearch: Boolean = false
    ): ProviderResponse

    suspend fun streamResponse(
        apiKey: String,
        modelId: String,
        messages: List<ChatMessagePayload>,
        endpoint: String? = null,
        temperature: Float = 0.7f,
        thinkingLevel: String? = null,
        enableWebSearch: Boolean = false,
        onChunkReceived: suspend (chunk: String) -> Unit
    ): ProviderResponse

    suspend fun validateApiKey(
        apiKey: String,
        endpoint: String? = null
    ): KeyValidationResult

    suspend fun listModels(
        apiKey: String,
        endpoint: String? = null
    ): List<AiModelInfo>

    fun getCapabilities(modelId: String): AiModelCapabilities
}
