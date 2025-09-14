import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.net.wifi.WifiManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import android.util.Log

class LohsController(private val appCtx: android.content.Context) {
    private val wifi by lazy { appCtx.applicationContext.getSystemService(WifiManager::class.java) }
    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null

    private fun hasFineLocation(): Boolean =
        ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun isSysLocationOn(): Boolean {
        val lm = appCtx.getSystemService(LocationManager::class.java)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm?.isLocationEnabled == true
        } else {
            Settings.Secure.getInt(
                appCtx.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            ) != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    /**
     * 开启本地热点（仅局域网）
     * - 需要：已授予 ACCESS_FINE_LOCATION & 系统定位总开关已开启
     */
    fun start(
        onStarted: (ssid: String, password: String) -> Unit = { _, _ -> },
        onFailed: (reason: Int, msg: String) -> Unit = { _, _ -> }
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            onFailed(-100, "系统版本低于 Android 8.0，无法开启本地热点")
            return
        }
        if (!hasFineLocation()) {
            onFailed(-101, "未授予定位权限")
            return
        }
        if (!isSysLocationOn()) {
            onFailed(-102, "系统定位总开关未开启")
            return
        }
        startApi26Plus(onStarted, onFailed)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("MissingPermission")
    private fun startApi26Plus(
        onStarted: (ssid: String, password: String) -> Unit,
        onFailed: (reason: Int, msg: String) -> Unit
    ) {
        try {
            wifi.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                override fun onStarted(r: WifiManager.LocalOnlyHotspotReservation) {
                    reservation = r // 必须持有引用，否则会被系统回收
                    val (ssid, pass) =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val c = r.softApConfiguration
                            (c?.ssid.orEmpty() to c?.passphrase.orEmpty())
                        } else {
                            @Suppress("DEPRECATION")
                            val c = r.wifiConfiguration
                            (c?.SSID.orEmpty() to c?.preSharedKey.orEmpty())
                        }
                    onStarted(ssid, pass)
                }

                override fun onStopped() {
                    reservation = null
                }

                override fun onFailed(reason: Int) {
                    onFailed(reason, "startLocalOnlyHotspot 回调失败：$reason")
                }
            }, Handler(Looper.getMainLooper()))
        } catch (se: SecurityException) {
            Log.e("LOHS", "SecurityException", se)
            onFailed(-201, "安全异常：请确认权限与定位开关（${se.message}）")
        } catch (ise: IllegalStateException) {
            Log.e("LOHS", "IllegalStateException", ise)
            onFailed(-202, "状态异常：系统拒绝开启热点（${ise.message}）")
        } catch (e: Exception) {
            Log.e("LOHS", "Unknown exception", e)
            onFailed(-299, "未知异常：${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stop() {
        reservation?.close()
        reservation = null
    }

    fun isRunning(): Boolean = reservation != null
}
