package com.flowstable.upi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flowstable.upi.ussd.USSDController
import com.flowstable.upi.ussd.UPIData
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PaymentActivity : AppCompatActivity() {

    private lateinit var etUpiId: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var btnPay: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        initViews()
        parseIntentData()
        setupClickListeners()
    }

    private fun initViews() {
        etUpiId = findViewById(R.id.etUpiId)
        etName = findViewById(R.id.etName)
        etAmount = findViewById(R.id.etAmount)
        btnPay = findViewById(R.id.btnPay)
    }

    private fun parseIntentData() {
        val upiString = intent.getStringExtra("upi_string") ?: return
        
        try {
            val uri = Uri.parse(upiString)
            val pa = uri.getQueryParameter("pa") // Payee address (UPI ID)
            val pn = uri.getQueryParameter("pn") // Payee name
            val am = uri.getQueryParameter("am") // Amount

            pa?.let { etUpiId.setText(it) }
            pn?.let { etName.setText(it) }
            am?.let { etAmount.setText(it) }
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        btnPay.setOnClickListener {
            val upiId = etUpiId.text?.toString()?.trim()
            val name = etName.text?.toString()?.trim() ?: ""
            val amount = etAmount.text?.toString()?.trim()

            if (upiId.isNullOrEmpty()) {
                etUpiId.error = "Enter UPI ID"
                return@setOnClickListener
            }

            if (amount.isNullOrEmpty()) {
                etAmount.error = "Enter amount"
                return@setOnClickListener
            }

            val amountDouble = amount.toDoubleOrNull()
            if (amountDouble == null || amountDouble <= 0) {
                etAmount.error = "Enter valid amount"
                return@setOnClickListener
            }

            // Store payment data and initiate USSD
            val upiData = UPIData(
                upiId = upiId,
                name = name,
                amount = amount
            )
            
            USSDController.currentPayment = upiData
            initiateUSSDPayment()
        }

        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun initiateUSSDPayment() {
        try {
            // Dial *99#
            val ussdCode = "*99${Uri.encode("#")}"
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$ussdCode")
            startActivity(intent)
            
            // Navigate to processing screen
            val processingIntent = Intent(this, ProcessingActivity::class.java)
            startActivity(processingIntent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initiate USSD: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
