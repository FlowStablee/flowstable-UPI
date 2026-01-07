package com.flowstable.upi

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
                        tvStatus.text = "Initializing..."
                    }
                    USSDController.State.MENU_MAIN -> {
                        tvStatus.text = "Navigating menu..."
                    }
                    USSDController.State.ENTER_UPI -> {
                        tvStatus.text = "Entering UPI ID..."
                    }
                    USSDController.State.ENTER_AMOUNT -> {
                        tvStatus.text = "Entering amount..."
                    }
                    USSDController.State.CONFIRM -> {
                        tvStatus.text = "Awaiting PIN entry..."
                    }
                    USSDController.State.SUCCESS -> {
                        navigateToResult(true)
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
        intent.putExtra("success", success)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
