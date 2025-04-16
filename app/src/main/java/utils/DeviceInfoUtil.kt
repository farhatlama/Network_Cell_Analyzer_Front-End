package utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getCellId(context: Context): Int {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cellInfoList = telephonyManager.allCellInfo
            val cellInfo: CellInfo? = cellInfoList?.firstOrNull()

            return when (cellInfo) {
                is CellInfoLte -> cellInfo.cellIdentity.ci
                is CellInfoGsm -> cellInfo.cellIdentity.cid
                is CellInfoWcdma -> cellInfo.cellIdentity.cid
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) {
                        val nrCell = cellInfo.cellIdentity as? CellIdentityNr
                        return nrCell?.nci?.toInt() ?: -1
                    } else {
                        return -1
                    }
                }

            }



        } catch (e: Exception) {
            return -1 // fallback if permission denied or error occurs
        }
    }

}