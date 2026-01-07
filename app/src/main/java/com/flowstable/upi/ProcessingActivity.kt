package com.flowstable.upi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flowstable.upi.ussd.USSDController

class ProcessingActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvPaymentDetails: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_processing)

        tvStatus = findViewById(R.id.tvStatus)
        tvPaymentDetails = findViewById(R.id.tvPaymentDetails)

        updatePaymentDetails()
        observeUSSDStatus()
    }

    private fun updatePaymentDetails() {
        val payment = USSDController.currentPayment
        if (payment != null) {
            tvPaymentDetails.text = "Paying ₹${payment.amount} to ${payment.name.ifEmpty { payment.upiId }}"
        }
    }

    private fun observeUSSDStatus() {
        // Poll USSD status
        handler.postDelayed(object : Runnable {
            override fun run() {
                when (USSDController.currentState) {
                    USSDController.State.IDLE -> {
                        tvStatus.text = "Initiating USSD Session..."
                    }
                    USSDController.State.SELECT_SIM -> {
                        tvStatus.text = "Detecting SIM Selection..."
                    }
                    USSDController.State.MENU_MAIN -> {
                        tvStatus.text = "Accessing BHIM *99#..."
                    }
                    USSDController.State.ENTER_UPI -> {
                        tvStatus.text = "Entering Recipient UPI ID..."
                    }
                    USSDController.State.ENTER_AMOUNT -> {
                        tvStatus.text = "Entering Amount..."
                    }
                    USSDController.State.CONFIRM -> {
                        tvStatus.text = "Waiting for UPI PIN..."
                    }
                    USSDController.State.SUCCESS -> {
                        navigateToResult(true)
                        return
                    }
                    USSDController.State.BALANCE_RESULT -> {
                        val intent = Intent(this@ProcessingActivity, ResultActivity::class.java).apply {
                            putExtra("is_success", true)
                            putExtra("message", "Your Bank Balance is: \nRs. ${USSDController.lastBalance}")
                        }
                        startActivity(intent)
                        finish()
                        return
                    }
                    USSDController.State.FAILED -> {
                        navigateToResult(false)
                        return
                    }
                }
                handler.postDelayed(this, 500)
            }
        }, 500)
    }

    private fun navigateToResult(success: Boolean) {
        val intent = android.content.Intent(this, ResultActivity::class.java)
        intent.putExtra("is_success", success) // Changed to "is_success" to match the new logic
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
