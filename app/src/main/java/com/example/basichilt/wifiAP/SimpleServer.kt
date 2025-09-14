package com.example.basichilt.wifiAP

// 需要 <uses-permission android:name="android.permission.INTERNET"/>
import android.util.Log
import timber.log.Timber
import java.io.IOException
import java.net.BindException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class SimpleServer(private val port: Int = 5555) {
    companion object { private const val TAG = "myAP" }

    private val pool = Executors.newCachedThreadPool()
    @Volatile private var running = false
    private var serverSocket: ServerSocket? = null

    fun start(): Boolean {
        // 1) 是否被快速“挡回去”
        if (running) {
            Log.w(TAG, "start() 被忽略：server 已在运行")
            return false
        }
        running = true

        Log.d(TAG, "start() 提交服务线程，port=$port")

        pool.execute {
            Log.d(TAG, "服务线程已启动，thread=${Thread.currentThread().name}")
            try {
                Log.d(TAG, "准备绑定端口 $port")
                serverSocket = ServerSocket(port)
                val ip = getApIpv4() ?: "未获取到"
                Log.i(TAG, "服务端启动成功：IP=$ip, 端口=$port")   // ← 你要看的这一行

                while (running) {
                    val s = serverSocket?.accept() ?: break
                    Log.d(TAG, "accept 到新连接: ${s.inetAddress.hostAddress}:${s.port}")
                    pool.execute { handle(s) }
                }
                Log.d(TAG, "服务线程正常退出")
            } catch (be: BindException) {
                Log.e(TAG, "端口已被占用: $port", be)
                running = false
            } catch (ioe: IOException) {
                Log.e(TAG, "ServerSocket I/O 异常", ioe)
                running = false
            } catch (e: Exception) {
                Log.e(TAG, "未知异常", e)
                running = false
            }
        }
        return true
    }


    private fun handle(s: Socket) {
        val peer = "${s.inetAddress.hostAddress}:${s.port}"
        Timber.tag("myAP").i("新连接：%s", peer)
        Log.d("myAP", "新连接：$peer")

        s.use { sock ->
            val inS = sock.getInputStream()
            val out = sock.getOutputStream().bufferedWriter(Charsets.UTF_8)
            val buf = ByteArray(4096)

            while (true) {
                val n = inS.read(buf)
                if (n <= 0) break
                val text = String(buf, 0, n, Charsets.UTF_8)
                Log.d("myAP", "接收到数据： $text")
                Timber.tag("myAP").i("← [%s] %s", peer, text)  // ⭐ 收到的数据（不需要换行）
                out.write("OK: $text")
                out.flush()
            }
        }

        Timber.tag("myAP").i("连接关闭：%s", peer)
    }


    fun stop() {
        running = false
        try { serverSocket?.close() } catch (_: Exception) {}
        pool.shutdownNow()
    }

    fun isRunning(): Boolean = running

    // 取 AP IPv4
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
