package com.example.basichilt.wifiAP

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)

        lohs = LohsController(this)
        btnStart = findViewById(R.id.btn_start_ap)
        btnStop = findViewById(R.id.btn_stop_ap)
        tvStatus = findViewById(R.id.tv_status)
        btnBack = findViewById(R.id.backtpPrevious)

        btnStart.setOnClickListener {
            // 权限判断
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), reqLocation)
            } else {
                startHotspot()
            }
        }

        btnStop.setOnClickListener {
            stopHotspot()
        }

        btnBack.setOnClickListener {
            var intent: Intent = Intent(this@WifiActivity, MainActivity::class.java)
            startActivity(intent)

        }
    }

    private fun startHotspot() {
        lohs.start(
            onStarted = { ssid, pass ->
                tvStatus.text = "状态：已开启\nSSID: $ssid\n密码: $pass"
                Toast.makeText(this, "热点已开：$ssid / $pass", Toast.LENGTH_LONG).show()
            },
            onFailed = { reason ->
                tvStatus.text = "状态：开启失败（$reason）"
                Toast.makeText(this, "开启失败：$reason", Toast.LENGTH_SHORT).show()
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopHotspot() {
        lohs.stop()
        tvStatus.text = "状态：已关闭"
        Toast.makeText(this, "热点已关", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        lohs.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == reqLocation && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startHotspot()
        }
    }
}
