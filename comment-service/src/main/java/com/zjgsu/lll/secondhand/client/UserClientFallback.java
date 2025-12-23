package com.zjgsu.lll.secondhand.client;

import com.zjgsu.lll.secondhand.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public Result<UserDTO> getUserById(Long id) {
        log.error("UserClient fallback triggered for getUserById, id: {}", id);
        return Result.error(500, "用户服务暂时不可用，请稍后重试");
    }
}
