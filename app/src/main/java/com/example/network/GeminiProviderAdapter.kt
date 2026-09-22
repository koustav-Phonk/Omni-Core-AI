package com.example.network

import com.example.BuildConfig
import com.example.model.AiModelCapabilities
import com.example.model.AiModelInfo
import com.example.model.AiProviderType
import com.example.model.ModelCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class GeminiProviderAdapter : AiProviderAdapter {
    override val providerType: AiProviderType = AiProviderType.GEMINI

    private fun resolveApiKey(userKey: String): String {
        return if (userKey.isNotBlank()) {
            userKey.trim()
        } else {
            BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        }
    }

    override suspend fun sendMessage(
        apiKey: String,
        modelId: String,
        messages: List<ChatMessagePayload>,
        endpoint: String?,
        temperature: Float,
        thinkingLevel: String?,
        enableWebSearch: Boolean
    ): ProviderResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val effectiveKey = resolveApiKey(apiKey)

        if (effectiveKey.isBlank() || effectiveKey == "MY_GEMINI_API_KEY") {
            // Intelligent local workstation simulation when key is not yet configured
            return@withContext ProviderResponse(
                text = "Nexus Workstation: Gemini adapter online. To connect to live Google servers, configure your API Key in the API Key Manager or Secrets panel.\n\nSimulated answer to: \"${messages.lastOrNull()?.content ?: ""}\"\n\nGemini 3.5 Flash and 3.1 Pro are ready for dispatch.",
                tokensCount = 65,
                latencyMs = 85
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = "$baseUrl/v1beta/models/$modelId:generateContent?key=$effectiveKey"

        try {
            val jsonBody = buildRequestBody(messages, temperature, thinkingLevel, enableWebSearch)
            val request = NetworkClient.createJsonPostRequest(url, jsonBody)
            val response = NetworkClient.client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = parseErrorMessage(errBody, response.code)
                return@withContext ProviderResponse(
                    text = "",
                    latencyMs = latency,
                    error = "Gemini API Error ($response.code): $errMessage"
                )
            }

            val bodyString = response.body?.string() ?: ""
            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var extractedText = ""
            var thinkingTrace: String? = null

            if (parts != null) {
                val sb = StringBuilder()
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.optBoolean("thought", false)) {
                        thinkingTrace = part.optString("text")
                    } else {
                        sb.append(part.optString("text", ""))
                    }
                }
                extractedText = sb.toString()
            }

            val usageMetadata = json.optJSONObject("usageMetadata")
            val totalTokens = usageMetadata?.optInt("totalTokenCount", extractedText.length / 4) ?: (extractedText.length / 4)

            ProviderResponse(
                text = extractedText.ifBlank { "No content returned from Gemini." },
                thinkingContent = thinkingTrace,
                tokensCount = totalTokens,
                latencyMs = latency
            )
        } catch (e: Exception) {
            ProviderResponse(
                text = "",
                latencyMs = System.currentTimeMillis() - startTime,
                error = "Network Failure: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    override suspend fun streamResponse(
        apiKey: String,
        modelId: String,
        messages: List<ChatMessagePayload>,
        endpoint: String?,
        temperature: Float,
        thinkingLevel: String?,
        enableWebSearch: Boolean,
        onChunkReceived: suspend (chunk: String) -> Unit
    ): ProviderResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val effectiveKey = resolveApiKey(apiKey)

        if (effectiveKey.isBlank() || effectiveKey == "MY_GEMINI_API_KEY") {
            val simulatedText = "Nexus AI (Gemini 3.5 Flash / 3.1 Pro Live Console):\n\nReceived instruction: \"${messages.lastOrNull()?.content ?: ""}\".\n\nAll neural parameters nominal. Connect your Gemini API key in API Key Manager for unlimited live queries."
            val words = simulatedText.split(" ")
            for (word in words) {
                kotlinx.coroutines.delay(25)
                onChunkReceived("$word ")
            }
            return@withContext ProviderResponse(
                text = simulatedText,
                tokensCount = simulatedText.length / 4,
                latencyMs = System.currentTimeMillis() - startTime
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = "$baseUrl/v1beta/models/$modelId:streamGenerateContent?alt=sse&key=$effectiveKey"

        try {
            val jsonBody = buildRequestBody(messages, temperature, thinkingLevel, enableWebSearch)
            val request = NetworkClient.createJsonPostRequest(url, jsonBody)
            val response = NetworkClient.client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = parseErrorMessage(errBody, response.code)
                return@withContext ProviderResponse(
                    text = "",
                    latencyMs = System.currentTimeMillis() - startTime,
                    error = "Gemini Stream Error ($response.code): $errMessage"
                )
            }

            val fullText = StringBuilder()
            var thinkingTrace: String? = null
            val inputStream = response.body?.byteStream()
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val jsonStr = currentLine.substring(6).trim()
                        if (jsonStr.isNotEmpty() && jsonStr != "[DONE]") {
                            try {
                                val chunkJson = JSONObject(jsonStr)
                                val candidates = chunkJson.optJSONArray("candidates")
                                val candidate = candidates?.optJSONObject(0)
                                val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
                                if (parts != null) {
                                    for (i in 0 until parts.length()) {
                                        val part = parts.getJSONObject(i)
                                        if (part.optBoolean("thought", false)) {
                                            thinkingTrace = (thinkingTrace ?: "") + part.optString("text", "")
                                        } else {
                                            val chunk = part.optString("text", "")
                                            if (chunk.isNotEmpty()) {
                                                fullText.append(chunk)
                                                onChunkReceived(chunk)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip malformed chunk
                            }
                        }
                    }
                }
            }

            ProviderResponse(
                text = fullText.toString(),
                thinkingContent = thinkingTrace,
                tokensCount = fullText.length / 4,
                latencyMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ProviderResponse(
                text = "",
                latencyMs = System.currentTimeMillis() - startTime,
                error = "Gemini Stream Failure: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    override suspend fun validateApiKey(apiKey: String, endpoint: String?): KeyValidationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val effectiveKey = resolveApiKey(apiKey)
        if (effectiveKey.isBlank() || effectiveKey == "MY_GEMINI_API_KEY") {
            return@withContext KeyValidationResult(
                isValid = false,
                latencyMs = 0,
                message = "No Gemini API Key provided. Please enter an API Key starting with AIza..."
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = "$baseUrl/v1beta/models?key=$effectiveKey"

        try {
            val request = NetworkClient.createGetRequest(url)
            val response = NetworkClient.client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val models = parseModelsList(body)
                KeyValidationResult(
                    isValid = true,
                    latencyMs = latency,
                    message = "Connected to Google Gemini (${latency}ms)",
                    discoveredModels = models
                )
            } else {
                KeyValidationResult(
                    isValid = false,
                    latencyMs = latency,
                    message = "Validation Failed (HTTP ${response.code})"
                )
            }
        } catch (e: Exception) {
            KeyValidationResult(
                isValid = false,
                latencyMs = System.currentTimeMillis() - startTime,
                message = "Connection Error: ${e.message}"
            )
        }
    }

    override suspend fun listModels(apiKey: String, endpoint: String?): List<AiModelInfo> = withContext(Dispatchers.IO) {
        val result = validateApiKey(apiKey, endpoint)
        if (result.discoveredModels.isNotEmpty()) {
            result.discoveredModels
        } else {
            ModelCatalog.getModelsForProvider(AiProviderType.GEMINI)
        }
    }

    override fun getCapabilities(modelId: String): AiModelCapabilities {
        return ModelCatalog.findModel(modelId)?.capabilities ?: AiModelCapabilities(hasVision = true)
    }

    private fun buildRequestBody(
        messages: List<ChatMessagePayload>,
        temperature: Float,
        thinkingLevel: String?,
        enableWebSearch: Boolean
    ): String {
        val root = JSONObject()
        val contentsArray = JSONArray()

        for (msg in messages) {
            val contentObj = JSONObject()
            val role = if (msg.role == "assistant") "model" else "user"
            contentObj.put("role", role)

            val partsArray = JSONArray()
            val textPart = JSONObject()
            textPart.put("text", msg.content)
            partsArray.put(textPart)

            if (!msg.imageBase64.isNullOrBlank()) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", msg.imageBase64)
                imagePart.put("inlineData", inlineData)
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }

        root.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", temperature)
        if (!thinkingLevel.isNullOrBlank()) {
            val thinkingObj = JSONObject()
            thinkingObj.put("thinkingLevel", thinkingLevel.lowercase())
            genConfig.put("thinkingConfig", thinkingObj)
        }
        root.put("generationConfig", genConfig)

        if (enableWebSearch) {
            val toolsArray = JSONArray()
            val googleSearchTool = JSONObject()
            googleSearchTool.put("googleSearch", JSONObject())
            toolsArray.put(googleSearchTool)
            root.put("tools", toolsArray)
        }

        return root.toString()
    }

    private fun parseErrorMessage(body: String, code: Int): String {
        return try {
            val json = JSONObject(body)
            val error = json.optJSONObject("error")
            error?.optString("message") ?: "HTTP error $code"
        } catch (e: Exception) {
            "HTTP error $code"
        }
    }

    private fun parseModelsList(body: String): List<AiModelInfo> {
        val models = mutableListOf<AiModelInfo>()
        try {
            val json = JSONObject(body)
            val modelsArray = json.optJSONArray("models") ?: return emptyList()
            for (i in 0 until modelsArray.length()) {
                val item = modelsArray.getJSONObject(i)
                val rawName = item.optString("name", "") // e.g. "models/gemini-3.5-flash"
                val id = rawName.removePrefix("models/")
                if (id.startsWith("gemini") && !id.contains("embedding") && !id.contains("aqa")) {
                    val displayName = item.optString("displayName", id)
                    val desc = item.optString("description", "")
                    val inputTokenLimit = item.optInt("inputTokenLimit", 1000000)
                    models.add(
                        AiModelInfo(
                            id = id,
                            provider = AiProviderType.GEMINI,
                            displayName = displayName,
                            contextLength = "${inputTokenLimit / 1000}K tokens",
                            description = desc,
                            capabilities = AiModelCapabilities(
                                hasVision = true,
                                hasThinking = id.contains("pro") || id.contains("thinking")
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return models
    }
}
