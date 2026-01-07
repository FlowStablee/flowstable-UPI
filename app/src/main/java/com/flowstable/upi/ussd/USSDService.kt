package com.flowstable.upi.ussd

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class USSDService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // TODO: Implement USSD dialog interception
    }

    override fun onInterrupt() {
    }
}
