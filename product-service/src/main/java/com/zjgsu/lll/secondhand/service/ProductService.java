package com.zjgsu.lll.secondhand.service;

import com.zjgsu.lll.secondhand.entity.Product;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found"));
    }

    public List<Product> getProductsByStatus(Integer status) {
        return productRepository.findByStatus(status);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    @Transactional
    public Product createProduct(Product product) {
        product.setStatus(1);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getProductById(id);

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setStock(product.getStock());
        existingProduct.setImageUrl(product.getImageUrl());

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public Product updateProductStatus(Long id, Integer status) {
        Product product = getProductById(id);
        product.setStatus(status);
        return productRepository.save(product);
    }

    @Transactional
    public void reduceStock(Long id, Integer quantity) {
        Product product = getProductById(id);

        if (product.getStock() < quantity) {
            throw new BusinessException("Insufficient stock");
        }

        product.setStock(product.getStock() - quantity);

        if (product.getStock() == 0) {
            product.setStatus(2);
        }

        productRepository.save(product);
    }
}
