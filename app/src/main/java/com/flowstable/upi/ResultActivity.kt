package com.flowstable.upi

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.flowstable.upi.ussd.USSDController
import com.google.android.material.button.MaterialButton

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val isSuccess = intent.getBooleanExtra("is_success", false)
        val customMessage = intent.getStringExtra("message")
        val payment = USSDController.currentPayment

        val ivStatus = findViewById<ImageView>(R.id.ivStatus)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvAmount = findViewById<TextView>(R.id.tvAmount)
        val btnDone = findViewById<MaterialButton>(R.id.btnDone)

        if (isSuccess) {
            ivStatus.setImageResource(R.drawable.ic_success)
            ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.success))
            tvTitle.text = "Payment Successful"
            tvMessage.text = customMessage ?: "Sent to ${payment?.name?.ifEmpty { payment.upiId } ?: "Unknown"}"
            tvAmount.text = "₹${payment?.amount ?: "0"}"
        } else {
            ivStatus.setImageResource(R.drawable.ic_error)
            ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.error))
            tvTitle.text = "Payment Failed"
            tvMessage.text = customMessage ?: "Please try again"
            tvAmount.visibility = android.view.View.GONE
        }

        btnDone.setOnClickListener {
            USSDController.reset()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
