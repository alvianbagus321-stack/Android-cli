package com.androidaiagent.agent

import com.androidaiagent.data.Provider

/** Provider configuration is intentionally server-backed; keys never belong in the APK. */
interface AIProviderClient { suspend fun complete(provider: Provider, model: String, prompt: String, imageBase64: String? = null): Result<String> }
class ProviderCatalog {
    fun defaults() = listOf(
        Provider("openai", "OpenAI / GPT", "openai-compatible", "https://api.openai.com/v1", listOf("gpt-4.1", "gpt-4.1-mini", "gpt-4o", "gpt-4o-mini", "o3", "o4-mini"), true, false),
        Provider("google", "Google Gemini", "gemini", "https://generativelanguage.googleapis.com/v1beta", listOf("gemini-2.5-pro", "gemini-3.1-pro", "gemini-3.6-flash", "gemini-2.5-flash"), true, false),
        Provider("deepseek", "DeepSeek", "openai-compatible", "https://api.deepseek.com/v1", listOf("deepseek-chat", "deepseek-reasoner", "deepseek-v3.1", "deepseek-r1"), false, false),
        Provider("anthropic", "Anthropic Claude", "anthropic", "https://api.anthropic.com/v1", listOf("claude-sonnet-4-20250514", "claude-opus-4-20250514", "claude-3-7-sonnet-20250219", "claude-3-5-haiku-20241022"), true, false),
        Provider("moonshot", "Kimi / Moonshot", "openai-compatible", "https://api.moonshot.ai/v1", listOf("kimi-k2", "kimi-k2-thinking", "moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"), false, false),
        Provider("minimax", "MiniMax", "openai-compatible", "https://api.minimax.io/v1", listOf("MiniMax-Text-01", "MiniMax-M2", "MiniMax-M2.1", "MiniMax-VL-01"), true, false),
        Provider("custom", "Custom / Local", "openai-compatible", "http://10.0.2.2:11434/v1", listOf("llama3.3", "qwen3", "mistral-small", "deepseek-r1", "local-model"), false, false)
    )
}
