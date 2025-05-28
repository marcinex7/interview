package com.example.refactortask.config;

import com.example.refactortask.model.entity.Category;
import com.example.refactortask.model.entity.Product;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
// Inconsistent annotation usage - using @Profile here but not in other config classes
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create categories
            Category electronics = new Category();
            electronics.setName("Electronics");
            electronics.setDescription("Electronic devices and gadgets");
            
            Category clothing = new Category();
            clothing.setName("Clothing");
            clothing.setDescription("Apparel and accessories");
            
            Category books = new Category();
            books.setName("Books");
            books.setDescription("Books and publications");
            
            // Save categories
            List<Category> categories = Arrays.asList(electronics, clothing, books);
            categoryRepository.saveAll(categories);
            
            // Create products
            Product laptop = Product.builder()
                    .productName("Laptop")
                    .description("High-performance laptop")
                    .productPrice(new BigDecimal("999.99"))
                    .stock_quantity(10)
                    .category(electronics)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isInStock(true)
                    .build();
            
            Product smartphone = Product.builder()
                    .productName("Smartphone")
                    .description("Latest smartphone model")
                    .productPrice(new BigDecimal("699.99"))
                    .stock_quantity(15)
                    .category(electronics)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isInStock(true)
                    .build();
            
            Product tShirt = Product.builder()
                    .productName("T-Shirt")
                    .description("Cotton t-shirt")
                    .productPrice(new BigDecimal("19.99"))
                    .stock_quantity(50)
                    .category(clothing)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isInStock(true)
                    .build();
            
            Product jeans = Product.builder()
                    .productName("Jeans")
                    .description("Denim jeans")
                    .productPrice(new BigDecimal("49.99"))
                    .stock_quantity(30)
                    .category(clothing)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isInStock(true)
                    .build();
            
            Product novel = Product.builder()
                    .productName("Novel")
                    .description("Bestselling novel")
                    .productPrice(new BigDecimal("14.99"))
                    .stock_quantity(20)
                    .category(books)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isInStock(true)
                    .build();
            
            Product textbook = Product.builder()
                    .productName("Textbook")
                    .description("Academic textbook")
                    .productPrice(new BigDecimal("79.99"))
                    .stock_quantity(0)
                    .category(books)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isInStock(false)
                    .build();
            
            // Save products
            List<Product> products = Arrays.asList(laptop, smartphone, tShirt, jeans, novel, textbook);
            productRepository.saveAll(products);
        };
    }
}