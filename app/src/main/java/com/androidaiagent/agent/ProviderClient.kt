package com.androidaiagent.agent

import com.androidaiagent.data.Provider

/** Provider configuration is intentionally server-backed; keys never belong in the APK. */
interface AIProviderClient { suspend fun complete(provider: Provider, model: String, prompt: String, imageBase64: String? = null): Result<String> }
class ProviderCatalog {
    fun defaults() = listOf(
        Provider("openai", "OpenAI / GPT", "openai-compatible", "https://api.openai.com/v1", listOf("gpt-4o", "gpt-4o-mini"), true, false),
        Provider("google", "Google Gemini", "gemini", "https://generativelanguage.googleapis.com/v1beta", listOf("gemini-2.0-flash", "gemini-2.5-pro"), true, false),
        Provider("deepseek", "DeepSeek", "openai-compatible", "https://api.deepseek.com/v1", listOf("deepseek-chat", "deepseek-reasoner"), false, false),
        Provider("anthropic", "Anthropic Claude", "anthropic", "https://api.anthropic.com/v1", listOf("claude-sonnet-4-20250514"), true, false),
        Provider("moonshot", "Kimi / Moonshot", "openai-compatible", "https://api.moonshot.ai/v1", listOf("kimi-k2"), false, false),
        Provider("minimax", "MiniMax", "openai-compatible", "https://api.minimax.io/v1", listOf("MiniMax-Text-01"), false, false),
        Provider("custom", "Custom / Local", "openai-compatible", "http://10.0.2.2:11434/v1", listOf("local-model"), false, false)
    )
}
