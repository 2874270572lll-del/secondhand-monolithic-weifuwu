package com.zjgsu.lll.secondhand.client;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;

@FeignClient(
        name = "product-service",
        fallbackFactory = ProductClientFallbackFactory.class,
        configuration = FeignConfig.class  // 添加这行
)
public interface ProductClient {
    @GetMapping("/products/{id}")
    Result<ProductDTO> getProductById(@PathVariable("id") Long id);

    @PutMapping("/products/{id}/reduce-stock/{quantity}")
    Result<Void> reduceStock(@PathVariable("id") Long id, @PathVariable("quantity") Integer quantity);
}

class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private Long sellerId;

    public ProductDTO() {
    }

    // getters and setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}