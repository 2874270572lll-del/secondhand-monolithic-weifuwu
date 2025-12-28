package com.zjgsu.lll.secondhand.gateway.filter;

import com.zjgsu.lll.secondhand.gateway.config.JwtProperties;
import com.zjgsu.lll.secondhand.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        log.info("=== JwtAuthenticationFilter initialized ===");
        log.info("JWT whitelist paths: {}", jwtProperties.getWhitelist());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        log.info("Processing request: {} {}", method, path);

        // 放行OPTIONS预检请求（CORS）
        if ("OPTIONS".equals(method)) {
            log.info("OPTIONS request, skipping JWT verification");
            return chain.filter(exchange);
        }

        // 检查是否在白名单中
        if (isWhitelisted(path)) {
            log.info("Path {} is whitelisted, skipping JWT verification", path);
            return chain.filter(exchange);
        }

        log.info("Path {} requires JWT verification", path);

        // 获取Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorized(exchange);
        }

        // 提取token
        String token = authHeader.substring(7);

        // 验证token
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for path: {}", path);
                return unauthorized(exchange);
            }

            // 从token中提取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                log.warn("Failed to extract username from token for path: {}", path);
                return unauthorized(exchange);
            }

            log.info("JWT verification successful for user: {}", username);

            // 将用户信息添加到请求头,传递给下游服务
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Name", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        List<String> whitelist = jwtProperties.getWhitelist();
        boolean result = whitelist.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (result) {
            log.info("Path {} matched whitelist pattern", path);
        }

        return result;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        String body = "{\"code\":401,\"message\":\"未授权,请先登录\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        // 优先级设置为-100,确保在其他过滤器之前执行
        return -100;
    }
}