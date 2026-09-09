package com.androidaiagent.agent

import com.androidaiagent.accessibility.AgentAccessibilityService

/** Structured, capability-aware Android actions for the future chat/tool-call loop. */
class AndroidToolExecutor {
    fun clickText(text: String): ToolResult = if (!AgentAccessibilityService.available()) unavailable("ACCESSIBILITY_UNAVAILABLE") else if (AgentAccessibilityService.clickText(text)) ok("click_text") else failure("click_text", "NODE_NOT_FOUND")
    fun typeText(text: String): ToolResult = if (!AgentAccessibilityService.available()) unavailable("ACCESSIBILITY_UNAVAILABLE") else if (AgentAccessibilityService.setText(text)) ok("type_text") else failure("type_text", "FOCUSED_EDITABLE_NODE_NOT_FOUND")
    fun tap(x: Float, y: Float): ToolResult = if (!AgentAccessibilityService.available()) unavailable("ACCESSIBILITY_UNAVAILABLE") else if (AgentAccessibilityService.tap(x, y)) ok("tap") else failure("tap", "GESTURE_REJECTED")
    fun scroll(forward: Boolean): ToolResult = if (!AgentAccessibilityService.available()) unavailable("ACCESSIBILITY_UNAVAILABLE") else if (if (forward) AgentAccessibilityService.scrollForward() else AgentAccessibilityService.scrollBackward()) ok("scroll") else failure("scroll", "SCROLLABLE_NODE_NOT_FOUND")
    private fun ok(tool: String) = ToolResult(true, tool)
    private fun failure(tool: String, code: String) = ToolResult(false, tool, errorCode = code, errorMessage = code)
    private fun unavailable(code: String) = ToolResult(false, "android_accessibility", errorCode = code, errorMessage = "Enable the Accessibility Service first")
}
