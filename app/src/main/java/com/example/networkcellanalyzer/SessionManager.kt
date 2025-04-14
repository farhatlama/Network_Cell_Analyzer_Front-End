// app/src/main/java/com/example/networkcellanalyzer/utils/SessionManager.kt
package com.example.networkcellanalyzer.utils

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
}