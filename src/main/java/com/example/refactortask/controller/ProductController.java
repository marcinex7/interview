package com.example.refactortask.controller;

import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Using ResponseEntity for all endpoints
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Additional endpoints with inconsistent URL patterns
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.findProductsByName(name));
    }

    @GetMapping("/price-less-than/{price}")
    public ResponseEntity<List<ProductDTO>> getProductsCheaperThan(@PathVariable BigDecimal price) {
        return ResponseEntity.ok(productService.findProductsCheaperThan(price));
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductDTO>> getProductsInStock() {
        return ResponseEntity.ok(productService.getProductsInStock());
    }
}