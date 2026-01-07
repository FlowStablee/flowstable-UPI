package com.flowstable.upi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        findViewById<MaterialCardView>(R.id.cardEnterUPI).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardServiceStatus).setOnClickListener {
            // Open accessibility settings
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Enable FlowStable USSD Service", Toast.LENGTH_LONG).show()
        }
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
        val statusIndicator = findViewById<android.view.View>(R.id.statusIndicator)
        val tvServiceStatus = findViewById<android.widget.TextView>(R.id.tvServiceStatus)
        val tvServiceHint = findViewById<android.widget.TextView>(R.id.tvServiceHint)

        if (isServiceEnabled) {
            statusIndicator.setBackgroundResource(R.drawable.circle_indicator)
            tvServiceStatus.text = "USSD Service Active"
            tvServiceHint.text = "Ready to process offline payments"
        } else {
            statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.warning))
            tvServiceStatus.text = "Service Not Enabled"
            tvServiceHint.text = "Tap to enable accessibility service"
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
