package com.zjgsu.lll.secondhand.client;

import com.zjgsu.lll.secondhand.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderClientFallback implements OrderClient {

    private static final Logger log = LoggerFactory.getLogger(OrderClientFallback.class);

    @Override
    public Result<OrderDTO> getOrderById(Long id) {
        log.error("OrderClient fallback triggered for getOrderById, id: {}", id);
        return Result.error(500, "订单服务暂时不可用，请稍后重试");
    }
}
