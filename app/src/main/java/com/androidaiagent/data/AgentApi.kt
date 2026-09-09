package com.androidaiagent.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Thin full-stack client. Use a relative/proxied URL in production; never ship provider keys here. */
class AgentApi(private val baseUrl: String, private val client: OkHttpClient = OkHttpClient()) {
    suspend fun health(): Result<String> = get("/health")
    suspend fun providers(): Result<String> = get("/api/providers")
    suspend fun tools(): Result<String> = get("/api/tools")
    suspend fun addTool(name: String, description: String, schema: JSONObject): Result<String> =
        post("/api/tools", JSONObject().apply { put("name", name); put("description", description); put("type", "prompt"); put("inputSchema", schema) })
    suspend fun chat(providerId: String, model: String, prompt: String, approvedScreen: Boolean = false): Result<String> =
        post("/api/chat", JSONObject().apply { put("providerId", providerId); put("model", model); put("prompt", prompt); put("approved", approvedScreen) })

    private suspend fun get(path: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching { client.newCall(Request.Builder().url(baseUrl.trimEnd('/') + path).get().build()).execute().use { response ->
            val body = response.body?.string().orEmpty(); if (!response.isSuccessful) error("HTTP ${response.code}: $body"); body
        } }
    }
    private suspend fun post(path: String, json: JSONObject): Result<String> = withContext(Dispatchers.IO) {
        runCatching { val body = json.toString().toRequestBody("application/json".toMediaType()); client.newCall(Request.Builder().url(baseUrl.trimEnd('/') + path).post(body).build()).execute().use { response ->
            val text = response.body?.string().orEmpty(); if (!response.isSuccessful) error("HTTP ${response.code}: $text"); text
        } }
    }
}
