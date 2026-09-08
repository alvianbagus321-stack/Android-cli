package com.androidaiagent.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.androidaiagent.agent.accessibilityFingerprint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Optional capability. Android will not grant control until the user enables this service. */
class AgentAccessibilityService : AccessibilityService() {
    companion object {
        private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 64)
        val events = _events.asSharedFlow()
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val nodes = rootInActiveWindow?.let { collect(it) } ?: emptyList()
        _events.tryEmit(UiEvent(event.eventType, event.packageName?.toString(), accessibilityFingerprint(nodes), System.currentTimeMillis()))
    }
    override fun onInterrupt() = Unit

    data class UiEvent(val type: Int, val packageName: String?, val accessibilityHash: String, val timestamp: Long)
    fun snapshot(): List<Map<String, Any?>> = rootInActiveWindow?.let { collect(it) } ?: emptyList()
    private fun collect(node: AccessibilityNodeInfo): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        val r = android.graphics.Rect(); node.getBoundsInScreen(r)
        result += mapOf("text" to node.text?.toString(), "contentDescription" to node.contentDescription?.toString(), "className" to node.className?.toString(), "resourceId" to node.viewIdResourceName, "clickable" to node.isClickable, "enabled" to node.isEnabled, "bounds" to listOf(r.left,r.top,r.right,r.bottom))
        for (i in 0 until node.childCount) node.getChild(i)?.let { result += collect(it); it.recycle() }
        return result
    }
}
