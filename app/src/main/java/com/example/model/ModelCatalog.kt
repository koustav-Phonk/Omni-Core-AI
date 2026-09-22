package com.example.model

object ModelCatalog {
    val defaultModels: List<AiModelInfo> = listOf(
        // Google Gemini Models (Supported modern preview versions)
        AiModelInfo(
            id = "gemini-3.5-flash",
            provider = AiProviderType.GEMINI,
            displayName = "Gemini 3.5 Flash",
            contextLength = "1M tokens",
            description = "Google's fastest multimodal workhorse for general reasoning, summarization and high-speed chat.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = false, hasWebSearch = true),
            isDefault = true
        ),
        AiModelInfo(
            id = "gemini-3.1-pro-preview",
            provider = AiProviderType.GEMINI,
            displayName = "Gemini 3.1 Pro (Thinking)",
            contextLength = "2M tokens",
            description = "Flagship Google model with advanced STEM reasoning, high thinking depth, and multimodal vision.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = true, hasWebSearch = true)
        ),
        AiModelInfo(
            id = "gemini-3.1-flash-lite-preview",
            provider = AiProviderType.GEMINI,
            displayName = "Gemini 3.1 Flash Lite",
            contextLength = "1M tokens",
            description = "Ultra low-latency, lightweight Gemini model designed for maximum response velocity.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = false, hasWebSearch = false)
        ),

        // OpenAI Models
        AiModelInfo(
            id = "gpt-4o",
            provider = AiProviderType.OPENAI,
            displayName = "GPT-4o (Omni)",
            contextLength = "128K tokens",
            description = "OpenAI's versatile multimodal flagship model with real-time text and vision capabilities.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = false, hasWebSearch = true),
            isDefault = true
        ),
        AiModelInfo(
            id = "gpt-4o-mini",
            provider = AiProviderType.OPENAI,
            displayName = "GPT-4o mini",
            contextLength = "128K tokens",
            description = "Affordable, high-speed lightweight intelligence for everyday tasks.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = false)
        ),
        AiModelInfo(
            id = "o3-mini",
            provider = AiProviderType.OPENAI,
            displayName = "o3-mini (Reasoning)",
            contextLength = "200K tokens",
            description = "High-precision STEM and coding reasoning model with chain-of-thought.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = true)
        ),

        // Anthropic Claude Models
        AiModelInfo(
            id = "claude-3-5-sonnet-20241022",
            provider = AiProviderType.ANTHROPIC,
            displayName = "Claude 3.5 Sonnet",
            contextLength = "200K tokens",
            description = "Anthropic's leading model for sophisticated writing, coding, nuance, and visual analysis.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = true),
            isDefault = true
        ),
        AiModelInfo(
            id = "claude-3-5-haiku-20241022",
            provider = AiProviderType.ANTHROPIC,
            displayName = "Claude 3.5 Haiku",
            contextLength = "200K tokens",
            description = "Blazing fast intelligence with near-Sonnet coding and reasoning performance.",
            capabilities = AiModelCapabilities(hasVision = true, hasThinking = false)
        ),

        // xAI Grok Models
        AiModelInfo(
            id = "grok-2-latest",
            provider = AiProviderType.XAI_GROK,
            displayName = "Grok 2",
            contextLength = "131K tokens",
            description = "State-of-the-art conversational model by xAI with real-time world knowledge.",
            capabilities = AiModelCapabilities(hasVision = true, hasWebSearch = true),
            isDefault = true
        ),
        AiModelInfo(
            id = "grok-beta",
            provider = AiProviderType.XAI_GROK,
            displayName = "Grok Beta",
            contextLength = "131K tokens",
            description = "High-throughput preview version of Grok with witty and uncensored insights.",
            capabilities = AiModelCapabilities(hasVision = false, hasWebSearch = true)
        ),

        // DeepSeek Models
        AiModelInfo(
            id = "deepseek-chat",
            provider = AiProviderType.DEEPSEEK,
            displayName = "DeepSeek V3",
            contextLength = "64K tokens",
            description = "Ultra-efficient 671B MoE architecture delivering frontier-level chat and programming.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = false),
            isDefault = true
        ),
        AiModelInfo(
            id = "deepseek-reasoner",
            provider = AiProviderType.DEEPSEEK,
            displayName = "DeepSeek R1",
            contextLength = "64K tokens",
            description = "Open reasoning model with transparent chain-of-thought verification trace.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = true)
        ),

        // Mistral Models
        AiModelInfo(
            id = "mistral-large-latest",
            provider = AiProviderType.MISTRAL,
            displayName = "Mistral Large",
            contextLength = "128K tokens",
            description = "European frontier model with exceptional multilingual comprehension and logic.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = false),
            isDefault = true
        ),
        AiModelInfo(
            id = "codestral-latest",
            provider = AiProviderType.MISTRAL,
            displayName = "Codestral",
            contextLength = "32K tokens",
            description = "Mistral's dedicated generative model for 80+ programming languages.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = false)
        ),

        // Meta Llama Models
        AiModelInfo(
            id = "meta-llama/llama-3.3-70b-instruct",
            provider = AiProviderType.META_LLAMA,
            displayName = "Llama 3.3 70B",
            contextLength = "128K tokens",
            description = "Meta's flagship open-weights model rivaling proprietary frontier models.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = false),
            isDefault = true
        ),
        AiModelInfo(
            id = "meta-llama/llama-3.1-405b-instruct",
            provider = AiProviderType.META_LLAMA,
            displayName = "Llama 3.1 405B",
            contextLength = "128K tokens",
            description = "The largest open model in existence, capable of synthetic data generation and complex reasoning.",
            capabilities = AiModelCapabilities(hasVision = false, hasThinking = false)
        )
    )

    fun getModelsForProvider(provider: AiProviderType): List<AiModelInfo> {
        return defaultModels.filter { it.provider == provider }
    }

    fun findModel(modelId: String): AiModelInfo? {
        return defaultModels.firstOrNull { it.id.equals(modelId, ignoreCase = true) }
    }

    fun getDefaultModelForProvider(provider: AiProviderType): AiModelInfo {
        return getModelsForProvider(provider).firstOrNull { it.isDefault }
            ?: getModelsForProvider(provider).firstOrNull()
            ?: defaultModels.first()
    }
}
