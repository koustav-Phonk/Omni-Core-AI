package com.example.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object NetworkClient {
    val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun createJsonPostRequest(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap()
    ): Request {
        val builder = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))

        headers.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
        return builder.build()
    }

    fun createGetRequest(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Request {
        val builder = Request.Builder()
            .url(url)
            .get()

        headers.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
        return builder.build()
    }
}
