// app/src/main/java/com/example/networkcellanalyzer/utils/SessionManager.kt
package com.example.networkcellanalyzer.utils
import android.content.Context
import android.content.SharedPreferences
import utils.DeviceInfoUtil


class SessionManager(private val context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "NetworkCellAnalyzerPrefs"
        const val USER_TOKEN = "user_token"
        const val USERNAME = "username"
        const val DEVICE_ID = "device_id"
        const val MAC_ADDRESS = "mac_address"
        const val IP_ADDRESS = "ip_address"
        const val CELL_ID = "cell_id"


    }

    init {
        // Auto-initialize device info if not already saved
        if (getDeviceId() == null) {
            saveDeviceId(DeviceInfoUtil.getDeviceId(context))
        }
        if (getMacAddress() == null) {
            saveMacAddress(DeviceInfoUtil.getMacAddress())
        }
        if (getIpAddress() == null) {
            saveIpAddress(DeviceInfoUtil.getIPAddress())
        }
    }

    // ─── Authentication ─────────────────────────
    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUsername(username: String) {
        prefs.edit().putString(USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return prefs.getString(USERNAME, null)
    }

    // ─── Device Info ─────────────────────────────
    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(DEVICE_ID, deviceId).apply()
    }

    fun getDeviceId(): String? {
        return prefs.getString(DEVICE_ID, null)
    }

    fun saveMacAddress(macAddress: String) {
        prefs.edit().putString(MAC_ADDRESS, macAddress).apply()
    }

    fun getMacAddress(): String? {
        return prefs.getString(MAC_ADDRESS, null)
    }

    private fun saveIpAddress(ipAddress: String) {
        prefs.edit().putString(IP_ADDRESS, ipAddress).apply()
    }

    fun getIpAddress(): String? {
        return prefs.getString(IP_ADDRESS, null)
    }

    fun saveCellId(cellId: String) {
        prefs.edit().putString(CELL_ID, cellId).apply()
    }

    fun getCellId(): String? {
        return prefs.getString(CELL_ID, null)
    }    // ─── Clear All ───────────────────────────────
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}



// app/src/main/java/com/example/networkcellanalyzer/utils/SessionManager.kt



/*package com.example.networkcellanalyzer.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "NetworkCellAnalyzerPrefs"
        const val USER_TOKEN = "user_token"
        const val USERNAME = "username"
        const val DEVICE_ID = "device_id"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUsername(username: String) {
        val editor = prefs.edit()
        editor.putString(USERNAME, username)
        editor.apply()
    }

    fun getUsername(): String? {
        return prefs.getString(USERNAME, null)
    }

    fun saveDeviceId(deviceId: String) {
        val editor = prefs.edit()
        editor.putString(DEVICE_ID, deviceId)
        editor.apply()
    }

    fun getDeviceId(): String? {
        return prefs.getString(DEVICE_ID, null)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}*/