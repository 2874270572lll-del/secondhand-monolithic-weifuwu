package com.zjgsu.lll.secondhand.client;

import com.zjgsu.lll.secondhand.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public Result<ProductDTO> getProductById(Long id) {
        log.error("ProductClient fallback triggered for getProductById, id: {}", id);
        return Result.error(500, "商品服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> reduceStock(Long id, Integer quantity) {
        log.error("ProductClient fallback triggered for reduceStock, id: {}, quantity: {}", id, quantity);
        return Result.error(500, "商品服务暂时不可用，库存减少失败");
    }
}
