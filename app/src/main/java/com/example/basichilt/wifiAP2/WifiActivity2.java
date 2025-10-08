//package com.example.basichilt.wifiAP2;
//
//import android.Manifest;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.LinkAddress;
//import android.net.LinkProperties;
//import android.net.Network;
//import android.net.wifi.SoftApConfiguration;
//import android.net.wifi.WifiManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.basichilt.R;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.IOException;
//import java.net.Inet4Address;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//
//public class WifiActivity2 extends AppCompatActivity {
//
//    private static final String SSID  = "MyDemoAP";
//    private static final String PWD   = "MyStrongPwd";
//    private static final int    PORT  = 9000;
//
//    private TextView tvLog;
//    private WifiManager wifiManager;
//    private WifiManager.LocalOnlyHotspotReservation reservation;
//
//    private final ExecutorService ioPool = Executors.newCachedThreadPool();
//    private volatile boolean serverRunning = false;
//
//    // 运行时权限申请
//    private final ActivityResultLauncher<String[]> permLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
//                // 点“开启热点”时也会再次尝试
//                append("权限结果：" + r.toString());
//            });
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_wifi2);
//
//        tvLog = findViewById(R.id.tv_log);
//        Button back = findViewById(R.id.backtomain);
//        Button btnStart = findViewById(R.id.btn_start_ap);
//        Button btnStop  = findViewById(R.id.btn_stop_ap);
//
//        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//
//        // 返回：推荐直接 finish()
//        back.setOnClickListener(v -> finish());
//        // 如果你确实想显式回到 MainActivity，用下面这三行替换：
////        back.setOnClickListener(v -> {
////            Intent intent = new Intent(WifiActivity2.this, MainActivity.class);
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
////            startActivity(intent);
////        });
//
//        btnStart.setOnClickListener(v -> {
//            requestPermsIfNeeded();
//            startHotspotAndServer();
//        });
//
//        btnStop.setOnClickListener(v -> stopAll());
//    }
//
//    private void requestPermsIfNeeded() {
//        if (Build.VERSION.SDK_INT >= 33) {
//            permLauncher.launch(new String[]{ Manifest.permission.NEARBY_WIFI_DEVICES });
//        } else {
//            permLauncher.launch(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION });
//        }
//    }
//
//    private void startHotspotAndServer() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
//            startHotspotWithConfig(SSID, PWD);
//        } else {
//            append("该设备 < Android 11，不能自定义 SSID/密码（会随机）。");
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.R)
//    private void startHotspotWithConfig(String ssid, String pass) {
//        SoftApConfiguration config = new SoftApConfiguration.Builder()
//                .setSsid(ssid)
//                .setPassphrase(pass, SoftApConfiguration.SECURITY_TYPE_WPA2_PSK)
//                .build();
//
//        wifiManager.startLocalOnlyHotspot(config, getMainExecutor(),
//                new WifiManager.LocalOnlyHotspotCallback() {
//                    @Override
//                    public void onStarted(WifiManager.LocalOnlyHotspotReservation r) {
//                        reservation = r;
//                        SoftApConfiguration cur = r.getSoftApConfiguration();
//                        append("热点已启动：SSID=" + cur.getSsid() + "  PWD=" + cur.getPassphrase());
//                        String ip = findApIpv4();
//                        append("本机热点IP=" + ip + "  端口=" + PORT);
//                        startTcpServer(PORT);
//                    }
//                    @Override
//                    public void onFailed(int reason) {
//                        append("启动热点失败，reason=" + reason);
//                    }
//                    @Override
//                    public void onStopped() {
//                        append("热点已停止");
//                    }
//                });
//    }
//
//    private void startTcpServer(int port) {
//        if (serverRunning) return;
//        serverRunning = true;
//        ioPool.execute(() -> {
//            try (ServerSocket server = new ServerSocket(port)) {
//                append("TCP 监听中: " + port + "，等待 NetAssist 连接…");
//                while (serverRunning) {
//                    Socket s = server.accept();
//                    append("客户端已连接：" + s.getInetAddress());
//                    ioPool.execute(() -> handleClient(s));
//                }
//            } catch (IOException e) {
//                append("TCP 服务异常: " + e);
//            }
//        });
//    }
//
//    private void handleClient(Socket s) {
//        try (BufferedInputStream in = new BufferedInputStream(s.getInputStream());
//             BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream())) {
//            byte[] buf = new byte[2048];
//            int n;
//            while ((n = in.read(buf)) != -1) {
//                byte[] data = new byte[n];
//                System.arraycopy(buf, 0, data, 0, n);
//                append("收到(" + n + "B)");
//                // 回显
//                out.write(data);
//                out.flush();
//            }
//        } catch (IOException e) {
//            append("连接关闭: " + e.getMessage());
//        } finally {
//            try { s.close(); } catch (Exception ignore) {}
//        }
//    }
//
//    private String findApIpv4() {
//        try {
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//            for (Network n : cm.getAllNetworks()) {
//                LinkProperties lp = cm.getLinkProperties(n);
//                if (lp == null || lp.getInterfaceName() == null) continue;
//                String ifn = lp.getInterfaceName();
//                // 常见：ap0 / softap0 / wlan0（不同厂商不同）
//                if (ifn.startsWith("ap") || ifn.contains("softap") || ifn.startsWith("wlan")) {
//                    for (LinkAddress la : lp.getLinkAddresses()) {
//                        if (la.getAddress() instanceof Inet4Address) {
//                            return ((Inet4Address) la.getAddress()).getHostAddress();
//                        }
//                    }
//                }
//            }
//        } catch (Exception ignore) {}
//        return "未知（可在客户端看默认网关）";
//    }
//
//    private void stopAll() {
//        serverRunning = false;
//        if (reservation != null) {
//            try { reservation.close(); } catch (Exception ignore) {}
//            reservation = null;
//        }
//        append("已停止热点与 TCP 服务");
//    }
//
//    private void append(String s) {
//        runOnUiThread(() -> {
//            if (tvLog != null) {
//                CharSequence old = tvLog.getText();
//                tvLog.setText((old == null ? "" : old.toString()) + (old == null || old.length()==0 ? "" : "\n") + s);
//            }
//        });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        stopAll();
//        ioPool.shutdownNow();
//    }
//}
