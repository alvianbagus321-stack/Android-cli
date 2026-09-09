package com.androidaiagent.agent

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/**
 * Optional user-authorized bridge. Shizuku is not root and this class never silently
 * starts it or bypasses Android permission checks.
 */
class ShizukuConnector {
    fun isServiceAvailable(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
    fun hasUserGrant(): Boolean = runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)
    fun canUse(): Boolean = isServiceAvailable() && hasUserGrant()
    fun requestUserGrant() { if (isServiceAvailable() && !hasUserGrant()) Shizuku.requestPermission(REQUEST_CODE) }

    /** Executes only an explicitly approved, non-interactive command after Shizuku grant. */
    suspend fun execute(command: List<String>, timeoutMs: Long = 10_000): Result<String> = withContext(Dispatchers.IO) {
        if (!canUse()) return@withContext Result.failure(IllegalStateException("SHIZUKU_PERMISSION_REQUIRED"))
        if (command.isEmpty() || command.any { it.any(Char::isWhitespace) && it.contains(";") }) return@withContext Result.failure(IllegalArgumentException("INVALID_COMMAND"))
        runCatching {
            val process = Shizuku.newProcess(command.toTypedArray(), null, null)
            val output = process.inputStream.bufferedReader().use { it.readText().take(MAX_OUTPUT) }
            process.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            if (process.exitValue() != 0) error("SHIZUKU_EXIT_${process.exitValue()}")
            output
        }
    }

    companion object { const val REQUEST_CODE = 2407; private const val MAX_OUTPUT = 128 * 1024 }
}
