package com.studycheckin.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

@Slf4j
@Component
public class AutoUpdateBaseUrlConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void updateMiniAppBaseUrl() {
        try {
            String lanIp = getLanIp();
            if (lanIp == null) {
                log.warn("[AutoUpdate] 未检测到局域网IP，跳过config.js更新");
                return;
            }

            // 小程序config.js路径（与后端同父目录）
            Path configPath = Path.of(System.getProperty("user.dir"))
                    .getParent()
                    .resolve("study-checkin-miniapp-master/config.js");

            if (!Files.exists(configPath)) {
                log.warn("[AutoUpdate] config.js不存在: {}", configPath);
                return;
            }

            String content = "// 开发环境接口地址（后端启动时自动更新）\n"
                    + "const BASE_URL = 'http://" + lanIp + ":8080'\n\n"
                    + "module.exports = {\n"
                    + "  BASE_URL\n"
                    + "}\n";

            Files.writeString(configPath, content);
            log.info("[AutoUpdate] config.js已更新 → http://{}:8080", lanIp);
        } catch (Exception e) {
            log.warn("[AutoUpdate] 更新config.js失败: {}", e.getMessage());
        }
    }

    private String getLanIp() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface net = nets.nextElement();
                if (net.isLoopback() || !net.isUp()) continue;
                Enumeration<InetAddress> addrs = net.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    String ip = addr.getHostAddress();
                    if (isPrivateIp(ip) && !ip.contains(":")) {
                        return ip;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isPrivateIp(String ip) {
        return ip.startsWith("192.168.") || ip.startsWith("10.") || ip.matches("172\\.(1[6-9]|2\\d|3[01])\\..*");
    }
}
