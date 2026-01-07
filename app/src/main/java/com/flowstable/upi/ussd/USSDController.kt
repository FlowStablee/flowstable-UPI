package com.flowstable.upi.ussd

object USSDController {

    enum class State {
        IDLE,
        MENU_MAIN,
        ENTER_UPI,
        ENTER_AMOUNT,
        CONFIRM,
        SUCCESS,
        FAILED
    }

    var currentState: State = State.IDLE
        private set

    var currentPayment: UPIData? = null

    fun updateState(newState: State) {
        currentState = newState
    }

    fun reset() {
        currentState = State.IDLE
        currentPayment = null
    }

    fun getNextInput(ussdText: String): String? {
        val payment = currentPayment ?: return null

        return when {
            // Main menu - select "Send Money" option
            ussdText.contains("Send Money", ignoreCase = true) ||
            ussdText.contains("1. Send", ignoreCase = true) -> {
                updateState(State.MENU_MAIN)
                "1" // Select Send Money
            }
            
            // UPI ID input
            ussdText.contains("Enter UPI", ignoreCase = true) ||
            ussdText.contains("VPA", ignoreCase = true) ||
            ussdText.contains("Mobile/UPI", ignoreCase = true) -> {
                updateState(State.ENTER_UPI)
                payment.upiId
            }
            
            // Amount input
            ussdText.contains("Enter Amount", ignoreCase = true) ||
            ussdText.contains("Amount", ignoreCase = true) -> {
                updateState(State.ENTER_AMOUNT)
                payment.amount
            }
            
            // Confirmation screen - needs PIN
            ussdText.contains("Confirm", ignoreCase = true) ||
            ussdText.contains("Enter PIN", ignoreCase = true) ||
            ussdText.contains("MPIN", ignoreCase = true) -> {
                updateState(State.CONFIRM)
                null // User must enter PIN manually for security
            }
            
            // Success markers
            ussdText.contains("successful", ignoreCase = true) ||
            ussdText.contains("Transaction ID", ignoreCase = true) -> {
                updateState(State.SUCCESS)
                null
            }
            
            // Failure markers
            ussdText.contains("failed", ignoreCase = true) ||
            ussdText.contains("error", ignoreCase = true) ||
            ussdText.contains("declined", ignoreCase = true) -> {
                updateState(State.FAILED)
                null
            }
            
            else -> null
        }
    }
}
