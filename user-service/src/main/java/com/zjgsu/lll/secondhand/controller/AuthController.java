package com.zjgsu.lll.secondhand.controller;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.dto.LoginRequest;
import com.zjgsu.lll.secondhand.dto.LoginResponse;
import com.zjgsu.lll.secondhand.entity.User;
import com.zjgsu.lll.secondhand.service.UserService;
import com.zjgsu.lll.secondhand.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("User login attempt: {}", loginRequest.getUsername());

        // ✅ 验证用户名和密码（内部使用 BCrypt）
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        if (user == null) {
            logger.warn("Login failed for user: {}", loginRequest.getUsername());
            return Result.error(401, "用户名或密码错误");
        }

        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        LoginResponse response = new LoginResponse(
                token,
                user.getUsername(),
                user.getId(),
                jwtExpiration
        );

        logger.info("User logged in successfully: {}", user.getUsername());
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        logger.info("User registration attempt: {}", user.getUsername());

        try {
            // ✅ save 方法内部会加密密码
            User savedUser = userService.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());

            // 不返回密码
            savedUser.setPassword(null);
            return Result.success(savedUser);
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return Result.error(400, "注册失败：" + e.getMessage());
        }
    }

    /**
     * 测试接口（无需认证）
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Auth service is running!");
    }
}