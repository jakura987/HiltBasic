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

/**
 * # SimpleServer
 *
 * 简单的 **TCP 服务端**（单进程内运行）：
 * - 在后台线程上 `bind(port)` 并 `listen()`；随后在 `accept()` 阻塞等待客户端连接。
 * - 每个新连接由 `handle(s)` 在线程池里异步处理：**循环读取 → 业务处理 → 回包**。
 * - `stop()` 会关闭监听 socket 并中断线程池。
 *
 * 典型用法（建议在 LOHS 成功回调里启动）：
 * ```
 * // onStarted(ssid, pass) 回调内：
 * if (server?.isRunning() != true) {
 *     server = SimpleServer(5556).also { it.start() }
 * }
 * ...
 * server?.stop() // 页面销毁或返回时调用
 * ```
 *
 * ⚠️ 注意：
 * - **只负责 LAN 内通信**：是否能互连，取决于对端是否加入同一 AP。
 * - 端口冲突（EADDRINUSE）常见于 ADB 无线调试占用 **5555**；建议用高位端口（如 5556/15555/5w+）。
 */
class SimpleServer(private val port: Int = 5556) {
    companion object { private const val TAG = "myAP" }

    private val pool = Executors.newCachedThreadPool()
    @Volatile private var running = false
    private var serverSocket: ServerSocket? = null

    private fun p(msg: String) {
        //Log.i(TAG, msg)                // ✅ Info 级别
        Timber.tag(TAG).i(msg)         // ✅ Timber
        //kotlin.io.println("$TAG: $msg")// ✅ System.out（Logcat 里也能看）
    }


    /**
     * 启动服务端监听：
     * - 放到线程池里运行，避免阻塞主线程。
     * - `ServerSocket` 绑定端口后，进入 `accept()` 循环等待连接。
     */
    fun start(): Boolean {
        if (running) { p("start() 忽略：已在运行"); return false }
        running = true
        pool.execute {
            try {
                p("服务线程启动，准备绑定端口 $port")

                // 更规范的 bind：先设置 reuseAddress，再绑定端口；backlog 可按需调整
                serverSocket = ServerSocket(port).apply { reuseAddress = true }
                p("服务端启动成功：IP=${getApIpv4() ?: "未获取到"} 端口=$port")

                // 主循环：阻塞等待连接；每个连接交给 handle() 处理
                while (running) {
                    p("accept() 等待连接中...")
                    val s = serverSocket?.accept() ?: break
                    p("accept 到新连接: ${s.inetAddress.hostAddress}:${s.port}")
                    pool.execute { handle(s) }
                }
                p("服务线程退出（running=$running）")
            } catch (e: Exception) {
                p("Server 线程异常：${e::class.simpleName} ${e.message}")
                running = false
            }
        }
        return true
    }


    /**
     * 处理“已建立”的单个 TCP 连接：
     * - 这里才是实际的收发数据通道（监听 socket 不收发数据）。
     * - 采用“行协议”演示：把收到的内容原样回显，并在末尾追加 CRLF（便于调试器按行显示）。
     *
     * @param s 已连接 socket（四元组唯一：clientIP, clientPort, serverIP, serverPort）
     */
    private fun handle(s: Socket) {
        val peer = "${s.inetAddress.hostAddress}:${s.port}"
        p("进入 handle(): $peer")
        try {
            s.tcpNoDelay = true
            s.keepAlive  = true

            val inS  = s.getInputStream()
            val outS = s.getOutputStream()
            val buf  = ByteArray(4096)

            while (true) {
                val n = inS.read(buf)
                if (n < 0) { p("对端关闭写端：$peer"); break }
                p("收到 $n 字节 ← [$peer]")
                val text = String(buf, 0, n, Charsets.UTF_8)
                p("内容：$text")
                outS.write("OK: ".toByteArray())
                outS.write(buf, 0, n)
                outS.write("\r\n".toByteArray())
                outS.flush()
                p("已回包 → [$peer] OK: $text")
            }
        } catch (e: Exception) {
            p("连接异常：$peer  ${e::class.simpleName} ${e.message}")
        } finally {
            try { s.close() } catch (_: Exception) {}
            p("连接关闭：$peer")
        }
    }



    fun stop() {
        running = false
        try { serverSocket?.close() } catch (_: Exception) {}
        pool.shutdownNow()
        p("stop() 调用完成")
    }

    fun isRunning(): Boolean = running

    /**
     * 获取 AP 接口的 IPv4：
     * - 优先 ap0（很多设备上 AP 接口名），其次 wlan0；
     * - 仅返回常见内网网段（10/172/192）。
     */
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
