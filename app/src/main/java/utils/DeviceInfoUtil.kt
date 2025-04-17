package utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Get operator name
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getOperatorName(context: Context): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operatorName = telephonyManager.networkOperatorName
            return if (operatorName.isNotEmpty()) operatorName else "Unknown"
        } catch (e: Exception) {
            return "Unknown"
        }
    }

    // Get network type
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getNetworkType(context: Context): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return getNetworkTypeString(telephonyManager.dataNetworkType)
        } catch (e: Exception) {
            return "Unknown"
        }
    }

    // Helper method to convert network type int to string
    private fun getNetworkTypeString(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA -> "2G"

            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G"

            TelephonyManager.NETWORK_TYPE_LTE -> "4G"

            TelephonyManager.NETWORK_TYPE_NR -> "5G"

            else -> "Unknown"
        }
    }

    // Get frequency band based on actual cell information
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getFrequencyBand(context: Context): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cellInfoList = telephonyManager.allCellInfo

            if (!cellInfoList.isNullOrEmpty()) {
                val cellInfo = cellInfoList[0]

                return when {
                    cellInfo is CellInfoLte -> {
                        val lteCell = cellInfo.cellIdentity as CellIdentityLte
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val earfcn = lteCell.earfcn
                            val band = getLteBandFromEarfcn(earfcn)
                            "$band MHz"
                        } else {
                            "LTE Band"
                        }
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr -> {
                        val nrCell = cellInfo.cellIdentity as CellIdentityNr
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val nrarfcn = nrCell.nrarfcn
                            val band = getNrBandFromNrarfcn(nrarfcn)
                            "$band MHz"
                        } else {
                            "5G Band"
                        }
                    }
                    cellInfo is CellInfoGsm -> "900/1800 MHz"
                    cellInfo is CellInfoWcdma -> "2100 MHz"
                    else -> "Unknown"
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceInfoUtil", "Error getting frequency band", e)
        }
        return "Unknown"
    }

    // Helper method to determine LTE band from EARFCN
    private fun getLteBandFromEarfcn(earfcn: Int): String {
        return when {
            earfcn in 0..599 -> "800"
            earfcn in 600..1199 -> "900"
            earfcn in 1200..1949 -> "1800"
            earfcn in 1950..2399 -> "1900"
            earfcn in 2400..2649 -> "2100"
            earfcn in 2650..2749 -> "850"
            earfcn in 2750..3449 -> "2600"
            earfcn in 3450..3799 -> "900"
            earfcn in 3800..4149 -> "1800"
            earfcn in 4150..4749 -> "700"
            earfcn in 4750..4999 -> "700"
            earfcn in 5000..5179 -> "850"
            earfcn in 5180..5279 -> "1900"
            earfcn in 5280..5379 -> "850"
            earfcn in 5730..5849 -> "2600"
            else -> "LTE"
        }
    }

    // Helper method to determine NR band from NRARFCN
    private fun getNrBandFromNrarfcn(nrarfcn: Int): String {
        return when {
            nrarfcn in 123400..130400 -> "850"
            nrarfcn in 143400..145600 -> "900"
            nrarfcn in 151600..160600 -> "1800"
            nrarfcn in 499200..537999 -> "3500"
            nrarfcn in 620000..680000 -> "4500"
            nrarfcn in 693334..733333 -> "26000"
            nrarfcn in 733334..748333 -> "28000"
            else -> "5G"
        }
    }

    // Get current timestamp
    fun getCurrentTimestamp(): String {
        val now = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(now)
    }

    // Get signal metrics (SINR and signal power)
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getSignalMetrics(context: Context): Pair<Double, Double> {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cellInfoList = telephonyManager.allCellInfo

            if (!cellInfoList.isNullOrEmpty()) {
                val signalStrength: Double
                val sinr: Double

                when (val cellInfo = cellInfoList[0]) {
                    is CellInfoLte -> {
                        // LTE metrics
                        signalStrength = cellInfo.cellSignalStrength.dbm.toDouble()
                        sinr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cellInfo.cellSignalStrength.rssnr.toDouble()
                        } else {
                            0.0
                        }
                    }
                    is CellInfoGsm -> {
                        // GSM metrics
                        signalStrength = cellInfo.cellSignalStrength.dbm.toDouble()
                        sinr = 0.0  // GSM doesn't have SINR
                    }
                    is CellInfoWcdma -> {
                        // WCDMA metrics
                        signalStrength = cellInfo.cellSignalStrength.dbm.toDouble()
                        sinr = 0.0  // Simplified
                    }
                    else -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) {
                            // 5G metrics
                            signalStrength = cellInfo.cellSignalStrength.dbm.toDouble()
                            sinr = 0.0  // Simplified
                        } else {
                            signalStrength = 0.0
                            sinr = 0.0
                        }
                    }
                }
                return Pair(sinr, signalStrength)
            }
        } catch (e: Exception) {
            Log.e("DeviceInfo", "Error getting signal metrics", e)
        }
        return Pair(0.0, 0.0)
    }

    // Format timestamp for API submission
    fun getFormattedTimestampForApi(): String {
        val now = Date()
        val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US)
        return formatter.format(now)
    }
}