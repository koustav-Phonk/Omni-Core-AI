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

class AnthropicProviderAdapter : AiProviderAdapter {
    override val providerType: AiProviderType = AiProviderType.ANTHROPIC

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
            val sampleResponse = "Nexus AI Workstation: Anthropic Claude adapter mounted.\n\nSimulated output for \"${messages.lastOrNull()?.content ?: ""}\" using $modelId.\n\nTo connect live, please enter your Anthropic API Key in the API Key Manager."
            return@withContext ProviderResponse(
                text = sampleResponse,
                tokensCount = sampleResponse.length / 4,
                latencyMs = 110
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = if (baseUrl.endsWith("/messages")) baseUrl else "$baseUrl/messages"

        try {
            val jsonBody = buildRequestBody(modelId, messages, temperature, stream = false)
            val headers = mapOf(
                "x-api-key" to apiKey.trim(),
                "anthropic-version" to "2023-06-01",
                "content-type" to "application/json"
            )

            val request = NetworkClient.createJsonPostRequest(url, jsonBody, headers)
            val response = NetworkClient.client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = parseErrorMessage(errBody, response.code)
                return@withContext ProviderResponse(
                    text = "",
                    latencyMs = latency,
                    error = "Claude Error ($response.code): $errMessage"
                )
            }

            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val contentArray = json.optJSONArray("content")
            val textBuilder = StringBuilder()
            if (contentArray != null) {
                for (i in 0 until contentArray.length()) {
                    val item = contentArray.getJSONObject(i)
                    if (item.optString("type") == "text") {
                        textBuilder.append(item.optString("text"))
                    }
                }
            }

            val usage = json.optJSONObject("usage")
            val inputTokens = usage?.optInt("input_tokens", 0) ?: 0
            val outputTokens = usage?.optInt("output_tokens", 0) ?: 0

            ProviderResponse(
                text = textBuilder.toString().ifBlank { "No content returned from Claude." },
                tokensCount = inputTokens + outputTokens,
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
            val simulatedText = "Nexus AI (Claude 3.5 Sonnet Workstation):\n\nProcessed: \"${messages.lastOrNull()?.content ?: ""}\".\n\nAll constitution constraints met. Configure your Anthropic API key in API Key Manager to connect directly to Claude."
            for (word in simulatedText.split(" ")) {
                kotlinx.coroutines.delay(28)
                onChunkReceived("$word ")
            }
            return@withContext ProviderResponse(
                text = simulatedText,
                tokensCount = simulatedText.length / 4,
                latencyMs = System.currentTimeMillis() - startTime
            )
        }

        val baseUrl = (endpoint?.takeIf { it.isNotBlank() } ?: providerType.defaultEndpoint).trimEnd('/')
        val url = if (baseUrl.endsWith("/messages")) baseUrl else "$baseUrl/messages"

        try {
            val jsonBody = buildRequestBody(modelId, messages, temperature, stream = true)
            val headers = mapOf(
                "x-api-key" to apiKey.trim(),
                "anthropic-version" to "2023-06-01",
                "content-type" to "application/json"
            )

            val request = NetworkClient.createJsonPostRequest(url, jsonBody, headers)
            val response = NetworkClient.client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = parseErrorMessage(errBody, response.code)
                return@withContext ProviderResponse(
                    text = "",
                    latencyMs = System.currentTimeMillis() - startTime,
                    error = "Claude Stream Error ($response.code): $errMessage"
                )
            }

            val fullText = StringBuilder()
            val inputStream = response.body?.byteStream()
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val data = currentLine.substring(6).trim()
                        if (data.isNotEmpty()) {
                            try {
                                val chunkJson = JSONObject(data)
                                val type = chunkJson.optString("type")
                                if (type == "content_block_delta") {
                                    val delta = chunkJson.optJSONObject("delta")
                                    val text = delta?.optString("text", "") ?: ""
                                    if (text.isNotEmpty()) {
                                        fullText.append(text)
                                        onChunkReceived(text)
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
                tokensCount = fullText.length / 4,
                latencyMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ProviderResponse(
                text = "",
                latencyMs = System.currentTimeMillis() - startTime,
                error = "Claude Stream Failure: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    override suspend fun validateApiKey(apiKey: String, endpoint: String?): KeyValidationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        if (apiKey.isBlank()) {
            return@withContext KeyValidationResult(
                isValid = false,
                latencyMs = 0,
                message = "Anthropic API Key required"
            )
        }

        // Validate by issuing a minimal test message request
        val testResponse = sendMessage(
            apiKey = apiKey,
            modelId = "claude-3-5-haiku-20241022",
            messages = listOf(ChatMessagePayload(role = "user", content = "ping")),
            endpoint = endpoint
        )

        val latency = System.currentTimeMillis() - startTime
        if (testResponse.error == null) {
            KeyValidationResult(
                isValid = true,
                latencyMs = latency,
                message = "Connected to Anthropic Claude (${latency}ms)"
            )
        } else {
            KeyValidationResult(
                isValid = false,
                latencyMs = latency,
                message = testResponse.error
            )
        }
    }

    override suspend fun listModels(apiKey: String, endpoint: String?): List<AiModelInfo> {
        return ModelCatalog.getModelsForProvider(AiProviderType.ANTHROPIC)
    }

    override fun getCapabilities(modelId: String): AiModelCapabilities {
        return ModelCatalog.findModel(modelId)?.capabilities ?: AiModelCapabilities(hasVision = true)
    }

    private fun buildRequestBody(
        modelId: String,
        messages: List<ChatMessagePayload>,
        temperature: Float,
        stream: Boolean
    ): String {
        val root = JSONObject()
        root.put("model", modelId)
        root.put("max_tokens", 4096)
        root.put("temperature", temperature)
        root.put("stream", stream)

        val msgArray = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("role", msg.role)

            if (!msg.imageBase64.isNullOrBlank()) {
                val contentArray = JSONArray()
                val imageObj = JSONObject()
                imageObj.put("type", "image")
                val sourceObj = JSONObject()
                sourceObj.put("type", "base64")
                sourceObj.put("media_type", "image/jpeg")
                sourceObj.put("data", msg.imageBase64)
                imageObj.put("source", sourceObj)
                contentArray.put(imageObj)

                val textObj = JSONObject()
                textObj.put("type", "text")
                textObj.put("text", msg.content)
                contentArray.put(textObj)

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
