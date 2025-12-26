package com.zjgsu.lll.secondhand.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置动态刷新演示Controller
 * 使用@RefreshScope注解实现配置的动态刷新
 */
@RestController
@RequestMapping("/api/config")
@RefreshScope  // 关键注解：支持配置动态刷新
public class ConfigController {

    // 从Nacos Config中读取的配置
    @Value("${business.feature.enabled:false}")
    private Boolean featureEnabled;

    @Value("${business.max-page-size:20}")
    private Integer maxPageSize;

    @Value("${business.welcome-message:欢迎使用二手交易平台}")
    private String welcomeMessage;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * 获取当前配置
     * 访问此接口可以查看当前从Nacos读取的配置值
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("featureEnabled", featureEnabled);
        config.put("maxPageSize", maxPageSize);
        config.put("welcomeMessage", welcomeMessage);
        config.put("jwtExpiration", jwtExpiration);
        config.put("jwtExpirationHours", jwtExpiration / 1000 / 3600);
        config.put("message", "配置从Nacos Config动态读取");
        config.put("refreshHint", "在Nacos控制台修改配置后，此接口将返回最新值（无需重启服务）");
        return config;
    }

    /**
     * 测试功能开关
     */
    @GetMapping("/feature-status")
    public Map<String, Object> getFeatureStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("featureEnabled", featureEnabled);
        result.put("status", featureEnabled ? "功能已启用" : "功能已禁用");
        return result;
    }

    /**
     * 获取欢迎消息
     */
    @GetMapping("/welcome")
    public Map<String, String> getWelcome() {
        Map<String, String> result = new HashMap<>();
        result.put("message", welcomeMessage);
        return result;
    }
}
