package com.flowstable.upi.ussd

object USSDController {

    enum class State {
        IDLE,
        SELECT_SIM,
        MENU_MAIN,
        ENTER_UPI,
        ENTER_AMOUNT,
        CONFIRM,
        SUCCESS,
        FAILED,
        BALANCE_RESULT
    }

    enum class Flow {
        PAYMENT,
        BALANCE
    }

    var currentState: State = State.IDLE
        private set

    var currentFlow: Flow = Flow.PAYMENT
    var currentPayment: UPIData? = null
    var lastBalance: String = ""
    var activeService: com.flowstable.upi.ussd.USSDService? = null

    fun updateState(newState: State) {
        currentState = newState
    }

    fun reset() {
        currentState = State.IDLE
        currentPayment = null
        lastBalance = ""
    }

    fun getNextInput(ussdText: String): String? {
        val text = ussdText.lowercase()

        // Handle SIM Selection if it appears as a USSD-like dialog
        if (text.contains("select sim") || text.contains("choose sim")) {
            updateState(State.SELECT_SIM)
            return "1" // Default to SIM 1
        }

        return when (currentFlow) {
            Flow.PAYMENT -> handlePaymentFlow(text)
            Flow.BALANCE -> handleBalanceFlow(text)
        }
    }

    private fun handlePaymentFlow(text: String): String? {
        val payment = currentPayment ?: return null

        return when {
            text.contains("send money") || text.contains("1. send") -> {
                updateState(State.MENU_MAIN)
                "1"
            }
            text.contains("enter upi") || text.contains("vpa") || text.contains("mobile/upi") -> {
                updateState(State.ENTER_UPI)
                payment.upiId
            }
            text.contains("enter amount") || text.contains("amount") -> {
                updateState(State.ENTER_AMOUNT)
                payment.amount
            }
            text.contains("confirm") || text.contains("enter pin") || text.contains("mpin") -> {
                updateState(State.CONFIRM)
                null
            }
            text.contains("successful") || text.contains("transaction id") -> {
                updateState(State.SUCCESS)
                null
            }
            text.contains("failed") || text.contains("error") || text.contains("declined") -> {
                updateState(State.FAILED)
                null
            }
            else -> null
        }
    }

    private fun handleBalanceFlow(text: String): String? {
        return when {
            text.contains("check balance") || text.contains("3. check") -> {
                updateState(State.MENU_MAIN)
                "3"
            }
            text.contains("enter pin") || text.contains("mpin") -> {
                updateState(State.CONFIRM)
                null
            }
            text.contains("balance is") || text.contains("rs.") -> {
                // Extract balance
                val regex = "rs\\.?\\s?([\\d.]+)".toRegex()
                val match = regex.find(text)
                lastBalance = match?.groupValues?.get(1) ?: text
                updateState(State.BALANCE_RESULT)
                null
            }
            else -> null
        }
    }
}
