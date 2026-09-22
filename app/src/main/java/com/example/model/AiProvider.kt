package com.example.model

enum class AiProviderType(
    val id: String,
    val displayName: String,
    val company: String,
    val defaultEndpoint: String,
    val keyPrefixHelp: String,
    val accentColorHex: Long
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        company = "Google DeepMind",
        defaultEndpoint = "https://generativelanguage.googleapis.com",
        keyPrefixHelp = "AIzaSy...",
        accentColorHex = 0xFF4285F4
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI",
        company = "OpenAI",
        defaultEndpoint = "https://api.openai.com/v1",
        keyPrefixHelp = "sk-proj-...",
        accentColorHex = 0xFF10A37F
    ),
    ANTHROPIC(
        id = "anthropic",
        displayName = "Anthropic Claude",
        company = "Anthropic",
        defaultEndpoint = "https://api.anthropic.com/v1",
        keyPrefixHelp = "sk-ant-...",
        accentColorHex = 0xFFD97706
    ),
    XAI_GROK(
        id = "xai",
        displayName = "xAI Grok",
        company = "xAI",
        defaultEndpoint = "https://api.x.ai/v1",
        keyPrefixHelp = "xai-...",
        accentColorHex = 0xFFFFFFFF
    ),
    DEEPSEEK(
        id = "deepseek",
        displayName = "DeepSeek",
        company = "DeepSeek",
        defaultEndpoint = "https://api.deepseek.com",
        keyPrefixHelp = "sk-...",
        accentColorHex = 0xFF4D6BFE
    ),
    MISTRAL(
        id = "mistral",
        displayName = "Mistral AI",
        company = "Mistral",
        defaultEndpoint = "https://api.mistral.ai/v1",
        keyPrefixHelp = "...",
        accentColorHex = 0xFFFF7000
    ),
    META_LLAMA(
        id = "meta_llama",
        displayName = "Meta Llama (OpenRouter)",
        company = "Meta / OpenRouter",
        defaultEndpoint = "https://openrouter.ai/api/v1",
        keyPrefixHelp = "sk-or-...",
        accentColorHex = 0xFF0081FB
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom Endpoint",
        company = "OpenAI Compatible",
        defaultEndpoint = "https://api.openai.com/v1",
        keyPrefixHelp = "sk-...",
        accentColorHex = 0xFF8B5CF6
    );

    companion object {
        fun fromId(id: String): AiProviderType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI
    }
}

data class AiModelCapabilities(
    val hasVision: Boolean = false,
    val hasThinking: Boolean = false,
    val hasCodeExecution: Boolean = true,
    val hasWebSearch: Boolean = false,
    val hasFunctionCalling: Boolean = true,
    val maxOutputTokens: Int = 8192
)

data class AiModelInfo(
    val id: String,
    val provider: AiProviderType,
    val displayName: String,
    val contextLength: String,
    val description: String,
    val capabilities: AiModelCapabilities = AiModelCapabilities(),
    val isDefault: Boolean = false,
    val customNickname: String? = null
)
