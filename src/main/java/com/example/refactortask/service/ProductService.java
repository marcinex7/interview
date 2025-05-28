package com.example.refactortask.service;

import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.model.entity.Product;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.repository.ProductRepository;
import com.example.refactortask.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        
        // Manually setting the category (inconsistent with mapper)
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }
        
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        productMapper.updateProductFromDto(productDTO, existingProduct);
        
        // Manually updating the category (inconsistent with mapper)
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // Additional business methods
    @Transactional(readOnly = true)
    public List<ProductDTO> findProductsByName(String name) {
        return productRepository.findByProductNameContainingIgnoreCase(name).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findProductsCheaperThan(BigDecimal price) {
        return productRepository.findProductsCheaperThan(price).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    // Inconsistent method naming (find vs get)
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsInStock() {
        return productRepository.findProductsInStock().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }
}