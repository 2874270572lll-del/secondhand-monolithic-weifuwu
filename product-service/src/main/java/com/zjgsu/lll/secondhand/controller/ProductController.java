package com.zjgsu.lll.secondhand.controller;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Product;
import com.zjgsu.lll.secondhand.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<List<Product>> getAllProducts() {
        return Result.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @GetMapping("/status/{status}")
    public Result<List<Product>> getProductsByStatus(@PathVariable Integer status) {
        return Result.success(productService.getProductsByStatus(status));
    }

    @GetMapping("/seller/{sellerId}")
    public Result<List<Product>> getProductsBySeller(@PathVariable Long sellerId) {
        return Result.success(productService.getProductsBySeller(sellerId));
    }

    @GetMapping("/category/{category}")
    public Result<List<Product>> getProductsByCategory(@PathVariable String category) {
        return Result.success(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public Result<List<Product>> searchProducts(@RequestParam String keyword) {
        return Result.success(productService.searchProducts(keyword));
    }

    @PostMapping
    public Result<Product> createProduct(@Valid @RequestBody Product product) {
        return Result.success(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return Result.success(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Product> updateProductStatus(@PathVariable Long id, @PathVariable Integer status) {
        return Result.success(productService.updateProductStatus(id, status));
    }

    @PutMapping("/{id}/reduce-stock/{quantity}")
    public Result<Void> reduceStock(@PathVariable Long id, @PathVariable Integer quantity) {
        productService.reduceStock(id, quantity);
        return Result.success();
    }
}
