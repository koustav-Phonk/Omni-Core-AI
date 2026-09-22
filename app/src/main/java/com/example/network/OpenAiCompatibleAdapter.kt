package com.example.network

import com.example.model.AiModelCapabilities
import com.example.model.AiModelInfo
import com.example.model.AiProviderType
import com.example.model.ModelCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class OpenAiCompatibleAdapter(
    override val providerType: AiProviderType
) : AiProviderAdapter {

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

        if (apiKey.isBlank()) {
            val sampleResponse = "Nexus AI Workstation: ${providerType.displayName} adapter mounted on bus.\n\nSimulated output for \"${messages.lastOrNull()?.content ?: ""}\" using $modelId.\n\nTo connect live, please enter your ${providerType.displayName} API Key in the API Key Manager."
            return@withContext ProviderResponse(
                text = sampleResponse,
                tokensCount = sampleResponse.length / 4,
                latencyMs = 95
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"

        try {
            val jsonBody = buildRequestBody(modelId, messages, temperature, stream = false)
            val headers = mutableMapOf(
                "Authorization" to "Bearer ${apiKey.trim()}",
                "Content-Type" to "application/json"
            )
            if (providerType == AiProviderType.META_LLAMA) {
                headers["HTTP-Referer"] = "https://nexus-ai-workstation.app"
                headers["X-Title"] = "Nexus AI Workstation"
            }

            val request = NetworkClient.createJsonPostRequest(url, jsonBody, headers)
            val response = NetworkClient.client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = parseErrorMessage(errBody, response.code)
                return@withContext ProviderResponse(
                    text = "",
                    latencyMs = latency,
                    error = "${providerType.displayName} Error ($response.code): $errMessage"
                )
            }

            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val message = firstChoice?.optJSONObject("message")
            val content = message?.optString("content", "") ?: ""
            val reasoning = message?.optString("reasoning_content", "")?.ifBlank { null }

            val usage = json.optJSONObject("usage")
            val totalTokens = usage?.optInt("total_tokens", content.length / 4) ?: (content.length / 4)

            ProviderResponse(
                text = content.ifBlank { "Empty response received." },
                thinkingContent = reasoning,
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

        if (apiKey.isBlank()) {
            val simulatedText = "Nexus AI (${providerType.displayName} - $modelId Console):\n\nQuery processed: \"${messages.lastOrNull()?.content ?: ""}\".\n\nNeural weights responded with zero anomalies. Configure your API key in the API Key Manager for unlimited live access."
            for (word in simulatedText.split(" ")) {
                kotlinx.coroutines.delay(30)
                onChunkReceived("$word ")
            }
            return@withContext ProviderResponse(
                text = simulatedText,
                tokensCount = simulatedText.length / 4,
                latencyMs = System.currentTimeMillis() - startTime
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"

        try {
            val jsonBody = buildRequestBody(modelId, messages, temperature, stream = true)
            val headers = mutableMapOf(
                "Authorization" to "Bearer ${apiKey.trim()}",
                "Content-Type" to "application/json"
            )
            if (providerType == AiProviderType.META_LLAMA) {
                headers["HTTP-Referer"] = "https://nexus-ai-workstation.app"
                headers["X-Title"] = "Nexus AI Workstation"
            }

            val request = NetworkClient.createJsonPostRequest(url, jsonBody, headers)
            val response = NetworkClient.client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = parseErrorMessage(errBody, response.code)
                return@withContext ProviderResponse(
                    text = "",
                    latencyMs = System.currentTimeMillis() - startTime,
                    error = "${providerType.displayName} Stream Error ($response.code): $errMessage"
                )
            }

            val fullText = StringBuilder()
            val thinkingTrace = StringBuilder()
            val inputStream = response.body?.byteStream()
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val data = currentLine.substring(6).trim()
                        if (data == "[DONE]") break
                        if (data.isNotEmpty()) {
                            try {
                                val chunkJson = JSONObject(data)
                                val choices = chunkJson.optJSONArray("choices")
                                val firstChoice = choices?.optJSONObject(0)
                                val delta = firstChoice?.optJSONObject("delta")
                                if (delta != null) {
                                    val reasoningDelta = delta.optString("reasoning_content", "")
                                    if (reasoningDelta.isNotEmpty()) {
                                        thinkingTrace.append(reasoningDelta)
                                    }
                                    val contentDelta = delta.optString("content", "")
                                    if (contentDelta.isNotEmpty()) {
                                        fullText.append(contentDelta)
                                        onChunkReceived(contentDelta)
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip unparseable chunk
                            }
                        }
                    }
                }
            }

            ProviderResponse(
                text = fullText.toString(),
                thinkingContent = thinkingTrace.toString().ifBlank { null },
                tokensCount = fullText.length / 4,
                latencyMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ProviderResponse(
                text = "",
                latencyMs = System.currentTimeMillis() - startTime,
                error = "${providerType.displayName} Stream Failure: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    override suspend fun validateApiKey(apiKey: String, endpoint: String?): KeyValidationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        if (apiKey.isBlank()) {
            return@withContext KeyValidationResult(
                isValid = false,
                latencyMs = 0,
                message = "API key is required"
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = "$baseUrl/models"

        try {
            val headers = mapOf("Authorization" to "Bearer ${apiKey.trim()}")
            val request = NetworkClient.createGetRequest(url, headers)
            val response = NetworkClient.client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                KeyValidationResult(
                    isValid = true,
                    latencyMs = latency,
                    message = "Connected to ${providerType.displayName} (${latency}ms)"
                )
            } else {
                val err = parseErrorMessage(response.body?.string() ?: "", response.code)
                KeyValidationResult(
                    isValid = false,
                    latencyMs = latency,
                    message = "Failed ($response.code): $err"
                )
            }
        } catch (e: Exception) {
            KeyValidationResult(
                isValid = false,
                latencyMs = System.currentTimeMillis() - startTime,
                message = "Error: ${e.localizedMessage}"
            )
        }
    }

    override suspend fun listModels(apiKey: String, endpoint: String?): List<AiModelInfo> {
        return ModelCatalog.getModelsForProvider(providerType)
    }

    override fun getCapabilities(modelId: String): AiModelCapabilities {
        return ModelCatalog.findModel(modelId)?.capabilities ?: AiModelCapabilities()
    }

    private fun buildRequestBody(
        modelId: String,
        messages: List<ChatMessagePayload>,
        temperature: Float,
        stream: Boolean
    ): String {
        val root = JSONObject()
        root.put("model", modelId)
        root.put("stream", stream)
        root.put("temperature", temperature)

        val msgArray = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("role", msg.role)

            if (!msg.imageBase64.isNullOrBlank()) {
                // OpenAI vision content array format
                val contentArray = JSONArray()
                val textObj = JSONObject()
                textObj.put("type", "text")
                textObj.put("text", msg.content)
                contentArray.put(textObj)

                val imageObj = JSONObject()
                imageObj.put("type", "image_url")
                val urlObj = JSONObject()
                urlObj.put("url", "data:image/jpeg;base64,${msg.imageBase64}")
                imageObj.put("image_url", urlObj)
                contentArray.put(imageObj)

                obj.put("content", contentArray)
            } else {
                obj.put("content", msg.content)
            }
            msgArray.put(obj)
        }
        root.put("messages", msgArray)
        return root.toString()
    }

    private fun parseErrorMessage(body: String, code: Int): String {
        return try {
            val json = JSONObject(body)
            val errObj = json.optJSONObject("error")
            errObj?.optString("message") ?: "HTTP error $code"
        } catch (e: Exception) {
            "HTTP error $code"
        }
    }
}
