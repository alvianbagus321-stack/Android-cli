package com.androidaiagent.data

data class Provider(val id: String, val name: String, val kind: String, val baseUrl: String, val models: List<String>, val vision: Boolean, val configured: Boolean)
data class Tool(val id: String, val name: String, val description: String, val inputSchema: String, val type: String, val enabled: Boolean = true)
data class Capability(val name: String, val status: String, val detail: String)
data class ToolResult(val success: Boolean, val tool: String, val output: String? = null, val errorCode: String? = null, val errorMessage: String? = null)
