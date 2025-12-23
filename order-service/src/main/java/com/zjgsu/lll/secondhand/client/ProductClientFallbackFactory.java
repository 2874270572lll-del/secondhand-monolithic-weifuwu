package com.zjgsu.lll.secondhand.client;

import com.zjgsu.lll.secondhand.common.Result;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {
            @Override
            public Result<ProductDTO> getProductById(Long id) {
                // 打印异常日志
                System.err.println("=== 触发降级 getProductById ===");
                System.err.println("异常类型: " + cause.getClass().getName());
                System.err.println("异常信息: " + cause.getMessage());

                // 返回降级响应
                return Result.error(503, "商品服务暂时不可用,请稍后重试");
            }

            @Override
            public Result<Void> reduceStock(Long id, Integer quantity) {
                System.err.println("=== 触发降级 reduceStock ===");
                System.err.println("异常信息: " + cause.getMessage());

                return Result.error(503, "库存服务暂时不可用,请稍后重试");
            }
        };
    }
}