// app/src/main/java/com/example/networkcellanalyzer/model/Models.kt
package com.example.networkcellanalyzer.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class SignupRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val message: String
)

data class CellRecordSubmission(
    val operator: String,
    val signal_power: Double,
    val sinr: Double,
    val network_type: String,
    val frequency_band: String,
    val cell_id: String,
    val timestamp: String,
    val device_mac: String,
    val device_id: String
)

data class NetworkData(
    val deviceId: String,
    val macAddress: String,
    val operator: String,
    val timestamp: String,
    val sinr: Double,
    val networkType: String,
    val frequencyBand: String,
    val cellId: String
)