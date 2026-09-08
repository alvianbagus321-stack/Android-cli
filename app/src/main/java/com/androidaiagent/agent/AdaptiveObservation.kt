package com.androidaiagent.agent

import java.security.MessageDigest
import kotlin.math.abs

/** Cheapest reliable source first; Vision is a conditional fallback, never the main loop. */
enum class ObservationLevel { NO_OBSERVATION, ACCESSIBILITY, LOCAL_ANALYSIS, SCREENSHOT, VISION_API }
enum class ObservationPriority { LOW, MEDIUM, HIGH, CRITICAL }
enum class ActionKind { WAIT, SHELL, TAP, TYPE_TEXT, SWIPE, OPEN_APP, SCREENSHOT, UNKNOWN }

data class ObservationConfig(
    val accessibilityConfidence: Double = .90,
    val localConfidence: Double = .85,
    val visionConfidence: Double = .70,
    val screenChangeRatio: Double = .08,
    val stateDebounceMs: Long = 350,
    val screenshotTtlMs: Long = 5_000,
    val maxVisionRequests: Int = 5,
    val maxScreenshots: Int = 10,
    val maxVisionBytes: Long = 8_000_000
)

data class ScreenState(
    val packageName: String?, val activityName: String?, val accessibilityHash: String?,
    val screenHash: String?, val orientation: Int, val width: Int, val height: Int,
    val timestamp: Long = System.currentTimeMillis(), val confidence: Double = 0.0
)

data class ObservationInput(
    val current: ScreenState?, val previous: ScreenState?, val action: ActionKind,
    val actionRisk: Double = 0.0, val accessibilityAvailable: Boolean,
    val accessibilityConfidence: Double, val localAnalysisAvailable: Boolean,
    val localConfidence: Double, val visionAvailable: Boolean,
    val priority: ObservationPriority = ObservationPriority.MEDIUM,
    val now: Long = System.currentTimeMillis()
)

data class ObservationDecision(val level: ObservationLevel, val reason: String, val confidence: Double = 0.0)

class ObservationPolicyEngine(private val config: ObservationConfig = ObservationConfig()) {
    fun decide(input: ObservationInput): ObservationDecision {
        if (input.action == ActionKind.WAIT || input.action == ActionKind.SHELL) return ObservationDecision(ObservationLevel.NO_OBSERVATION, "action_result_is_sufficient")
        if (input.accessibilityAvailable && input.accessibilityConfidence >= config.accessibilityConfidence && input.action != ActionKind.SCREENSHOT)
            return ObservationDecision(ObservationLevel.ACCESSIBILITY, "accessibility_confidence_high", input.accessibilityConfidence)
        if (input.localAnalysisAvailable && input.localConfidence >= config.localConfidence)
            return ObservationDecision(ObservationLevel.LOCAL_ANALYSIS, "local_analysis_confidence_high", input.localConfidence)
        val changed = input.previous == null || stateChanged(input.previous, input.current, config.screenChangeRatio)
        if (!changed && input.current?.screenHash != null) return ObservationDecision(ObservationLevel.NO_OBSERVATION, "state_unchanged_cached", input.current.confidence)
        if (input.action == ActionKind.SCREENSHOT || input.priority >= ObservationPriority.HIGH || input.actionRisk >= .8) {
            if (input.visionAvailable) return ObservationDecision(ObservationLevel.VISION_API, "visual_context_or_risk_requires_stronger_verification")
            return ObservationDecision(ObservationLevel.SCREENSHOT, "vision_unavailable_capture_for_local_fallback")
        }
        return if (input.visionAvailable) ObservationDecision(ObservationLevel.SCREENSHOT, "screen_changed_capture_before_escalation") else ObservationDecision(ObservationLevel.ACCESSIBILITY, "fallback_to_accessibility")
    }
    private fun stateChanged(a: ScreenState?, b: ScreenState?, ratio: Double): Boolean {
        if (a == null || b == null) return true
        return a.packageName != b.packageName || a.activityName != b.activityName || a.orientation != b.orientation || a.accessibilityHash != b.accessibilityHash || (a.screenHash != b.screenHash && hashDifference(a.screenHash, b.screenHash) >= ratio)
    }
}

class ScreenStateManager(private val config: ObservationConfig = ObservationConfig()) {
    private var last: ScreenState? = null
    fun current(): ScreenState? = last
    fun update(next: ScreenState): Boolean {
        val old = last
        if (old != null && next.timestamp - old.timestamp < config.stateDebounceMs && !significantChange(old, next)) return false
        last = next
        return old == null || significantChange(old, next)
    }
    fun significantChange(old: ScreenState?, next: ScreenState?): Boolean {
        if (old == null || next == null) return true
        return old.packageName != next.packageName || old.activityName != next.activityName || old.orientation != next.orientation || old.accessibilityHash != next.accessibilityHash || (old.screenHash != next.screenHash && hashDifference(old.screenHash, next.screenHash) >= config.screenChangeRatio)
    }
    fun reset() { last = null }
}

data class CachedScreenshot(val bytes: ByteArray, val packageName: String?, val activity: String?, val screenHash: String, val accessibilityHash: String?, val timestamp: Long, val confidence: Double)
class ScreenshotCache(private val ttlMs: Long = 5_000) {
    private var value: CachedScreenshot? = null
    fun get(state: ScreenState, now: Long = System.currentTimeMillis()): CachedScreenshot? = value?.takeIf { now - it.timestamp <= ttlMs && it.packageName == state.packageName && it.activity == state.activity && it.screenHash == state.screenHash && it.accessibilityHash == state.accessibilityHash }
    fun put(item: CachedScreenshot) { value = item }
    fun clear() { value = null }
}

class VisionResultCache(private val ttlMs: Long = 60_000) {
    private val values = mutableMapOf<String, Pair<Long, String>>()
    fun key(screenHash: String, intent: String, context: String = "") = sha256("$screenHash|$intent|$context")
    fun get(key: String, now: Long = System.currentTimeMillis()): String? = values[key]?.takeIf { now - it.first <= ttlMs }?.second
    fun put(key: String, value: String, now: Long = System.currentTimeMillis()) { values[key] = now to value }
    fun clear() = values.clear()
}

data class VisionUsage(val requests: Int, val cached: Int, val skipped: Int, val bytes: Long, val estimatedTokens: Long)
class VisionUsageManager(private val config: ObservationConfig = ObservationConfig()) {
    private var requests = 0; private var cached = 0; private var skipped = 0; private var bytes = 0L; private var tokens = 0L
    fun canRequest(imageBytes: Int): Boolean = requests < config.maxVisionRequests && bytes + imageBytes <= config.maxVisionBytes
    fun recordRequest(imageBytes: Int, estimatedTokens: Int) { if (canRequest(imageBytes)) { requests++; bytes += imageBytes; tokens += estimatedTokens } }
    fun recordCacheHit() { cached++ }
    fun recordSkipped() { skipped++ }
    fun exhausted() = requests >= config.maxVisionRequests || bytes >= config.maxVisionBytes
    fun snapshot() = VisionUsage(requests, cached, skipped, bytes, tokens)
    fun reset() { requests = 0; cached = 0; skipped = 0; bytes = 0; tokens = 0 }
}

fun accessibilityFingerprint(nodes: List<Map<String, Any?>>): String = sha256(nodes.joinToString("|") { node -> listOf("text", "contentDescription", "resourceId", "className", "clickable", "enabled", "selected", "focused", "bounds").joinToString(":") { node[it]?.toString().orEmpty() } })
fun imageFingerprint(bytes: ByteArray): String = sha256(bytes.decodeToString().ifEmpty { bytes.joinToString(",") })
private fun hashDifference(a: String?, b: String?): Double {
    if (a == null || b == null || a.length != b.length) return 1.0
    if (a == b) return 0.0
    return a.zip(b).count { it.first != it.second }.toDouble() / a.length
}
private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
