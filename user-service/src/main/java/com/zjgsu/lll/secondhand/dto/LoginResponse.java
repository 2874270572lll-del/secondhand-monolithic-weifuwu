package com.zjgsu.lll.secondhand.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {

    private String token;
    private String username;
    private Long userId;
    private Long expiresIn; // 过期时间（毫秒）

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, Long userId, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
