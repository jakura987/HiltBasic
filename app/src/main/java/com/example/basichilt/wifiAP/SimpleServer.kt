package com.example.basichilt.wifiAP

// 需要 <uses-permission android:name="android.permission.INTERNET"/>
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class SimpleServer(private val port: Int = 5555) {
    private val pool = Executors.newCachedThreadPool()
    @Volatile private var running = false

    fun start() {
        running = true
        pool.execute {
            ServerSocket(port).use { server ->
                Log.i("AP", "本机热点IP: ${getApIpv4() ?: "未知"}，端口: $port")
                while (running) {
                    val s = server.accept()
                    pool.execute { handle(s) }
                }
            }
        }
    }

    private fun handle(s: Socket) {
        s.use { sock ->
            val reader = sock.getInputStream().bufferedReader()
            val writer = sock.getOutputStream().bufferedWriter()
            var line = reader.readLine()
            while (line != null) {
                // 选一种写法（这里用写法B）
                writer.write("OK: $line\n")
                writer.flush()

                line = reader.readLine()
            }
        }
    }

    fun stop() {
        running = false
        pool.shutdownNow()
    }

    fun getApIpv4(): String? {
        val ifs = NetworkInterface.getNetworkInterfaces().toList()
        val preferred = ifs.firstOrNull { it.name.equals("ap0", true) }
            ?: ifs.firstOrNull { it.name.equals("wlan0", true) }
        val candidates = if (preferred != null) listOf(preferred) + ifs else ifs

        for (ni in candidates) {
            if (!ni.isUp || ni.isLoopback) continue
            for (addr in ni.inetAddresses) {
                if (addr is Inet4Address && !addr.isLoopbackAddress) {
                    val ip = addr.hostAddress
                    if (ip.startsWith("192.") || ip.startsWith("10.") || ip.startsWith("172.")) return ip
                }
            }
        }
        return null
    }
}
