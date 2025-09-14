package com.example.basichilt.wifiAP

import LohsController
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.basichilt.R
import com.example.basichilt.module.MainActivity
import timber.log.Timber

class WifiActivity : AppCompatActivity() {

    private lateinit var lohs: LohsController
    private val reqLocation = 1001


    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    private lateinit var btnBack: Button

    // 新增：服务端
    private var server: SimpleServer? = null
    private val serverPort = 5555
    private val uiHandler = Handler(Looper.getMainLooper())
    private val bg = java.util.concurrent.Executors.newSingleThreadExecutor()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)

        lohs = LohsController(this)
        btnStart = findViewById(R.id.btn_start_ap)
        btnStop  = findViewById(R.id.btn_stop_ap)
        tvStatus = findViewById(R.id.tv_status)
        btnBack  = findViewById(R.id.backtpPrevious)

        btnStart.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), reqLocation)
            } else {
                startHotspot()
            }
        }

        btnStop.setOnClickListener { stopHotspot() }

        btnBack.setOnClickListener {
            startActivity(Intent(this@WifiActivity, MainActivity::class.java))
        }
    }

    private fun startHotspot() {
        Timber.d("开启中...")
        Toast.makeText(this, "开启中...", Toast.LENGTH_LONG).show()
        lohs.start(
            onStarted = { ssid, pass ->
                // 1) 启动服务端（只启一次）
                if (server?.isRunning() != true) {
                    server = SimpleServer(serverPort).also {
                        val ok = it.start()
                        Log.d("myAP", "start() 已调用，返回=$ok")
                    }
                }
                // 2) 先显示基础信息
                tvStatus.text = "状态：已开启\nSSID: $ssid\n密码: $pass\nAP IP: 获取中...\n端口: $serverPort"
                Toast.makeText(this, "热点已开：$ssid / $pass", Toast.LENGTH_LONG).show()

                bg.execute {
                    var ip: String? = null
                    repeat(6) {  // 最多重试 6 次 * 300ms ≈ 1.8s
                        ip = server?.getApIpv4()
                        if (ip != null) return@repeat
                        try { Thread.sleep(300) } catch (_: InterruptedException) {}
                    }
                    val finalIp = ip ?: "未知"
                    uiHandler.post {
                        tvStatus.text = "状态：已开启\nSSID: $ssid\n密码: $pass\nAP IP: $finalIp\n端口: $serverPort"
                    }
                }
            },
            onFailed = { code, msg ->
                tvStatus.text = "状态：开启失败（$code）\n$msg"
                Toast.makeText(this, "开启失败：$msg（$code）", Toast.LENGTH_LONG).show()
            }
        )
    }



//    private fun startHotspot() {
//        lohs.start(
//            onStarted = { ssid, pass ->
//                tvStatus.text = "状态：已开启\nSSID: $ssid\n密码: $pass"
//                Toast.makeText(this, "热点已开：$ssid / $pass", Toast.LENGTH_LONG).show()
//            },
//            onFailed = { code, msg ->
//                tvStatus.text = "状态：开启失败（$code）\n$msg"
//                Toast.makeText(this, "开启失败：$msg（$code）", Toast.LENGTH_LONG).show()
//            }
//        )
//    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopHotspot() {
        // 先停服务，再关热点
        server?.stop()
        server = null

        lohs.stop()
        tvStatus.text = "状态：已关闭"
        Toast.makeText(this, "热点已关", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        // 页面销毁也做收尾
        server?.stop()
        server = null

        lohs.stop()
        bg.shutdownNow()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == reqLocation && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startHotspot()
        }
    }
}

