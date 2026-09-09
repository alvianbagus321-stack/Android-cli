package com.androidaiagent.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.androidaiagent.agent.accessibilityFingerprint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** User-enabled Android control layer. Every method returns false when the service is unavailable. */
class AgentAccessibilityService : AccessibilityService() {
    companion object {
        private var instance: AgentAccessibilityService? = null
        private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 64)
        val events = _events.asSharedFlow()
        fun available() = instance != null
        fun clickText(text: String) = instance?.clickTextInternal(text) ?: false
        fun setText(text: String) = instance?.setTextInternal(text) ?: false
        fun scrollForward() = instance?.scrollInternal(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        fun scrollBackward() = instance?.scrollInternal(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
        fun tap(x: Float, y: Float): Boolean {
            val service = instance ?: return false
            val path = Path().apply { moveTo(x, y) }
            return service.dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 80)).build(), null, null)
        }
    }
    data class UiEvent(val type: Int, val packageName: String?, val accessibilityHash: String, val timestamp: Long)
    override fun onServiceConnected() { instance = this; super.onServiceConnected() }
    override fun onDestroy() { instance = null; super.onDestroy() }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { event ?: return; val nodes = rootInActiveWindow?.let { collect(it) } ?: emptyList(); _events.tryEmit(UiEvent(event.eventType, event.packageName?.toString(), accessibilityFingerprint(nodes), System.currentTimeMillis())) }
    override fun onInterrupt() = Unit
    fun snapshot(): List<Map<String, Any?>> = rootInActiveWindow?.let { collect(it) } ?: emptyList()
    private fun clickTextInternal(text: String): Boolean = find(rootInActiveWindow) { it.text?.toString() == text || it.contentDescription?.toString() == text }?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
    private fun setTextInternal(text: String): Boolean { val node = find(rootInActiveWindow) { it.isFocused && it.isEditable } ?: return false; return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }) }
    private fun scrollInternal(action: Int): Boolean = find(rootInActiveWindow) { it.isScrollable }?.performAction(action) == true
    private fun find(node: AccessibilityNodeInfo?, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? { if (node == null) return null; if (predicate(node)) return node; for (i in 0 until node.childCount) find(node.getChild(i), predicate)?.let { return it }; return null }
    private fun collect(node: AccessibilityNodeInfo): List<Map<String, Any?>> { val result = mutableListOf<Map<String, Any?>>(); val r = android.graphics.Rect(); node.getBoundsInScreen(r); result += mapOf("text" to node.text?.toString(), "contentDescription" to node.contentDescription?.toString(), "className" to node.className?.toString(), "resourceId" to node.viewIdResourceName, "clickable" to node.isClickable, "enabled" to node.isEnabled, "selected" to node.isSelected, "focused" to node.isFocused, "bounds" to listOf(r.left, r.top, r.right, r.bottom)); for (i in 0 until node.childCount) node.getChild(i)?.let { result += collect(it); it.recycle() }; return result }
}
