package com.zjgsu.lll.secondhand.controller;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Product;
import com.zjgsu.lll.secondhand.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Value("${server.port:8082}")
    private String serverPort;

    // 获取容器 hostname 作为实例标识
    private final String instanceId;

    // 缓存容器IP地址
    private final String containerIp;

    public ProductController(ProductService productService) {
        this.productService = productService;
        // 从环境变量获取实例 ID，如果没有则使用容器 hostname
        this.instanceId = System.getenv("INSTANCE_ID") != null
                ? System.getenv("INSTANCE_ID")
                : System.getenv("HOSTNAME");

        // 获取容器的实际 IP 地址
        this.containerIp = getContainerIp();
    }

    /**
     * 获取容器的 IP 地址
     */
    private String getContainerIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("无法获取容器IP地址", e);
            return "Unknown";
        }
    }

    @GetMapping
    public Result<List<Product>> getAllProducts(HttpServletResponse response) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: GET /products", containerIp, instanceId, serverPort);
        // 添加实例标识到响应头，用于负载均衡测试
        response.setHeader("X-Instance-Id", instanceId != null ? instanceId : "unknown");
        response.setHeader("X-Instance-IP", containerIp);
        response.setHeader("X-Server-Port", serverPort);
        return Result.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: GET /products/{}", containerIp, instanceId, serverPort, id);
        return Result.success(productService.getProductById(id));
    }

    @GetMapping("/status/{status}")
    public Result<List<Product>> getProductsByStatus(@PathVariable Integer status) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: GET /products/status/{}", containerIp, instanceId, serverPort, status);
        return Result.success(productService.getProductsByStatus(status));
    }

    @GetMapping("/seller/{sellerId}")
    public Result<List<Product>> getProductsBySeller(@PathVariable Long sellerId) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: GET /products/seller/{}", containerIp, instanceId, serverPort, sellerId);
        return Result.success(productService.getProductsBySeller(sellerId));
    }

    @GetMapping("/category/{category}")
    public Result<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: GET /products/category/{}", containerIp, instanceId, serverPort, category);
        return Result.success(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public Result<List<Product>> searchProducts(@RequestParam String keyword) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: GET /products/search?keyword={}", containerIp, instanceId, serverPort, keyword);
        return Result.success(productService.searchProducts(keyword));
    }

    @PostMapping
    public Result<Product> createProduct(@Valid @RequestBody Product product) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: POST /products", containerIp, instanceId, serverPort);
        return Result.success(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: PUT /products/{}", containerIp, instanceId, serverPort, id);
        return Result.success(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: DELETE /products/{}", containerIp, instanceId, serverPort, id);
        productService.deleteProduct(id);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Product> updateProductStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: PUT /products/{}/status/{}", containerIp, instanceId, serverPort, id, status);
        return Result.success(productService.updateProductStatus(id, status));
    }

    @PutMapping("/{id}/reduce-stock/{quantity}")
    public Result<Void> reduceStock(@PathVariable Long id, @PathVariable Integer quantity) {
        log.info("🔵 [IP:{}] [{}:{}] 处理请求: PUT /products/{}/reduce-stock/{}",
                containerIp, instanceId, serverPort, id, quantity);
        productService.reduceStock(id, quantity);
        return Result.success();
    }
}