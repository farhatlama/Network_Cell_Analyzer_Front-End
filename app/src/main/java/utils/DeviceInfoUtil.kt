package utils

import android.content.Context
import android.provider.Settings
import java.net.Inet4Address
import java.net.NetworkInterface

object DeviceInfoUtil {

    // Secure device ID
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // MAC Address (will be dummy on Android 10+)
    fun getMacAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val nif = interfaces.nextElement()
                val mac = nif.hardwareAddress
                if (mac != null) {
                    return mac.joinToString(":") { String.format("%02X", it) }
                }
            }
            "02:00:00:00:00:00"
        } catch (e: Exception) {
            "02:00:00:00:00:00"
        }
    }

    // IPv4 Address (e.g., 192.168.x.x)
    fun getIPAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val nif = interfaces.nextElement()
                val addresses = nif.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }
}