package com.flowstable.upi.ussd

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class USSDService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Only process window state changes (USSD dialogs)
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }

        val source = event.source ?: return
        
        // Check if this is a USSD dialog
        if (!isUSSDDialog(source)) {
            source.recycle()
            return
        }

        // Extract text from USSD dialog
        val ussdText = extractUSSDText(source)
        if (ussdText.isEmpty()) {
            source.recycle()
            return
        }

        // Get the next input from controller
        val nextInput = USSDController.getNextInput(ussdText)
        
        if (nextInput != null) {
            // Automatically fill the input
            fillUSSDInput(source, nextInput)
            clickSendButton(source)
        }

        source.recycle()
    }

    private fun isUSSDDialog(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: ""
        val packageName = node.packageName?.toString() ?: ""
        
        // Common USSD dialog indicators
        return packageName.contains("com.android.phone") ||
               packageName.contains("com.samsung.android.phone") ||
               packageName.contains("telephony") ||
               className.contains("AlertDialog") ||
               className.contains("UssdAlertActivity")
    }

    private fun extractUSSDText(node: AccessibilityNodeInfo): String {
        val textBuilder = StringBuilder()
        extractTextRecursive(node, textBuilder)
        return textBuilder.toString()
    }

    private fun extractTextRecursive(node: AccessibilityNodeInfo, builder: StringBuilder) {
        node.text?.let {
            builder.append(it).append(" ")
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractTextRecursive(child, builder)
            child.recycle()
        }
    }

    private fun fillUSSDInput(root: AccessibilityNodeInfo, text: String) {
        val editTexts = root.findAccessibilityNodeInfosByClassName("android.widget.EditText")
        
        for (editText in editTexts) {
            if (editText.isEditable) {
                val args = Bundle()
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            }
            editText.recycle()
        }
    }

    private fun clickSendButton(root: AccessibilityNodeInfo) {
        // Find and click the Send/OK button
        val buttons = root.findAccessibilityNodeInfosByClassName("android.widget.Button")
        
        for (button in buttons) {
            val buttonText = button.text?.toString()?.lowercase() ?: ""
            if (buttonText.contains("send") || 
                buttonText.contains("ok") || 
                buttonText.contains("reply")) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                button.recycle()
                return
            }
            button.recycle()
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service connected and ready
    }
}
