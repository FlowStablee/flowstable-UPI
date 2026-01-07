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
            // Automatically fill the input if editable field exists
            val inputFilled = fillUSSDInput(source, nextInput)
            
            if (inputFilled) {
                clickSendButton(source)
            } else {
                // If no input field, maybe it's a direct choice (like SIM selection)
                // Try to find a button/text that matches the choice
                clickMatchingOption(source, nextInput)
            }
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

    private fun fillUSSDInput(root: AccessibilityNodeInfo, text: String): Boolean {
        val editTexts = ArrayList<AccessibilityNodeInfo>()
        findNodesByClassName(root, "android.widget.EditText", editTexts)
        
        var filled = false
        for (editText in editTexts) {
            if (editText.isEditable) {
                val args = Bundle()
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                filled = true
            }
            // Don't recycle here as they are needed for list, allow system to handle or recycle later if strictly managing
        }
        return filled
    }

    private fun clickMatchingOption(root: AccessibilityNodeInfo, choice: String) {
        // Look for buttons or text views that might be the choice
        val nodes = ArrayList<AccessibilityNodeInfo>()
        findClickableNodes(root, nodes)
        
        for (node in nodes) {
            val text = node.text?.toString()?.lowercase() ?: ""
            if (text == choice.lowercase() || text.contains("sim ${choice.lowercase()}") || text.startsWith("${choice.lowercase()}.")) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
    }

    private fun findClickableNodes(root: AccessibilityNodeInfo, outList: MutableList<AccessibilityNodeInfo>) {
        if (root.isClickable) {
            outList.add(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            findClickableNodes(child, outList)
            child.recycle() // Recycle child after use
        }
    }

    private fun clickSendButton(root: AccessibilityNodeInfo) {
        val buttons = ArrayList<AccessibilityNodeInfo>()
        findNodesByClassName(root, "android.widget.Button", buttons)
        
        for (button in buttons) {
            val buttonText = button.text?.toString()?.lowercase() ?: ""
            if (buttonText.contains("send") || 
                buttonText.contains("ok") || 
                buttonText.contains("reply")) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
    }

    private fun findNodesByClassName(root: AccessibilityNodeInfo, className: String, outList: MutableList<AccessibilityNodeInfo>) {
        if (root.className?.toString() == className) {
            outList.add(root)
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            findNodesByClassName(child, className, outList)
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
