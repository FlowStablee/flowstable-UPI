package com.flowstable.upi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flowstable.upi.ussd.USSDController
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val PHONE_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupClickListeners()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun setupClickListeners() {
        findViewById<MaterialCardView>(R.id.cardScanQR).setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this, ScannerActivity::class.java))
            } else {
                requestCameraPermission()
            }
        }

        findViewById<View>(R.id.cardEnterUPI).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        findViewById<View>(R.id.cardCheckBalance).setOnClickListener {
            checkBalance()
        }

        findViewById<View>(R.id.cardVoicePay).setOnClickListener {
            // NPCI 123PAY Service
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:08045163666")
            }
            if (checkPhonePermission()) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Phone permission needed for Voice Pay", Toast.LENGTH_SHORT).show()
                requestPhonePermission()
            }
        }

        findViewById<View>(R.id.cardServiceStatus).setOnClickListener {
            // Open accessibility settings
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Enable FlowStable USSD Service", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkBalance() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Please enable Accessibility Service first", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        USSDController.reset()
        USSDController.currentFlow = USSDController.Flow.BALANCE
        
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:" + Uri.encode("*99#"))
        }
        
        // Start processing activity first so it's ready to observe
        startActivity(Intent(this, ProcessingActivity::class.java))
        startActivity(intent)
    }

    private fun checkPermissions() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
        }
        if (!checkPhonePermission()) {
            requestPhonePermission()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun requestPhonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CALL_PHONE),
            PHONE_PERMISSION_CODE
        )
    }

    private fun updateServiceStatus() {
        val isServiceEnabled = isAccessibilityServiceEnabled()
        val btnServiceStatus = findViewById<com.google.android.material.button.MaterialButton>(R.id.cardServiceStatus)

        if (isServiceEnabled) {
            btnServiceStatus.text = "Service Active"
            btnServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
            btnServiceStatus.setIconResource(R.drawable.ic_check_circle)
            btnServiceStatus.iconTint = androidx.core.content.ContextCompat.getColorStateList(this, R.color.success)
        } else {
            btnServiceStatus.text = "Check Status"
            btnServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.accent))
            btnServiceStatus.setIconResource(R.drawable.ic_arrow_right)
            btnServiceStatus.iconTint = androidx.core.content.ContextCompat.getColorStateList(this, R.color.accent)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${packageName}/${packageName}.ussd.USSDService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(serviceName)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                }
            }
            PHONE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Phone permission granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
