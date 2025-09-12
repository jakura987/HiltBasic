package com.example.basichilt.wifiAP

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi

class LohsController(private val appCtx: android.content.Context) {
    private val wifi by lazy { appCtx.applicationContext.getSystemService(WifiManager::class.java) }
    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null

    /**
     * 开启本地热点（仅局域网）
     * 需要：已授予 ACCESS_FINE_LOCATION 且系统定位开关已打开
     */
    fun start(
        onStarted: (ssid: String, password: String) -> Unit = { _, _ -> },
        onFailed: (reason: Int) -> Unit = {}
    ) {
        // < 26 没有 LOHS 能力，直接回调失败/提示
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            onFailed(-1) // 自定义的错误码
            return
        }
        startApi26Plus(onStarted, onFailed) // 真正启动放到 26+ 的方法里，避免新 API 告警
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("MissingPermission") // 确保你已做运行时权限和“定位开关”判断
    private fun startApi26Plus(
        onStarted: (ssid: String, password: String) -> Unit,
        onFailed: (reason: Int) -> Unit
    ) {
        wifi.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
            override fun onStarted(r: WifiManager.LocalOnlyHotspotReservation) {
                reservation = r // 必须持有引用，否则热点会被系统立即回收

                // Android 11+ 用 softApConfiguration；Android 10 及以下用 wifiConfiguration
                val (ssid, pass) =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val c = r.softApConfiguration
                        c?.ssid.orEmpty() to (c?.passphrase.orEmpty())
                    } else {
                        @Suppress("DEPRECATION")
                        val c = r.wifiConfiguration
                        c?.SSID.orEmpty() to (c?.preSharedKey.orEmpty())
                    }

                onStarted(ssid, pass)
            }

            override fun onStopped() {
                reservation = null
            }

            override fun onFailed(reason: Int) {
                onFailed(reason)
            }
        }, Handler(Looper.getMainLooper()))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stop() {
        reservation?.close()
        reservation = null
    }

    fun isRunning(): Boolean = reservation != null
}
