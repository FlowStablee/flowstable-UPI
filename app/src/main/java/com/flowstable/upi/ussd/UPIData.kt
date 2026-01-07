package com.flowstable.upi.ussd

data class UPIData(
    val upiId: String,
    val name: String = "",
    val amount: String
)
