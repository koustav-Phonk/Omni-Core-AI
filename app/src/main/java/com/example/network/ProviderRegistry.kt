package com.example.network

import com.example.model.AiProviderType

object ProviderRegistry {
    private val adapters: Map<AiProviderType, AiProviderAdapter> = mapOf(
        AiProviderType.GEMINI to GeminiProviderAdapter(),
        AiProviderType.OPENAI to OpenAiCompatibleAdapter(AiProviderType.OPENAI),
        AiProviderType.ANTHROPIC to AnthropicProviderAdapter(),
        AiProviderType.XAI_GROK to OpenAiCompatibleAdapter(AiProviderType.XAI_GROK),
        AiProviderType.DEEPSEEK to OpenAiCompatibleAdapter(AiProviderType.DEEPSEEK),
        AiProviderType.MISTRAL to OpenAiCompatibleAdapter(AiProviderType.MISTRAL),
        AiProviderType.META_LLAMA to OpenAiCompatibleAdapter(AiProviderType.META_LLAMA),
        AiProviderType.CUSTOM to OpenAiCompatibleAdapter(AiProviderType.CUSTOM)
    )

    fun getAdapter(providerType: AiProviderType): AiProviderAdapter {
        return adapters[providerType] ?: adapters.getValue(AiProviderType.GEMINI)
    }

    fun getAllAdapters(): List<AiProviderAdapter> {
        return adapters.values.toList()
    }
}
