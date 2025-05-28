package com.example.refactortask.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Using @Getter and @Setter instead of @Data like in Product
@Entity
@Table(name = "CATEGORIES") // Inconsistent table naming (uppercase vs lowercase)
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Inconsistent with Product's IDENTITY strategy
    private Long categoryId; // Inconsistent naming (should be id like in Product)

    @Column(nullable = false, unique = true)
    private String name;

    // No column annotation, inconsistent with Product
    private String description;

    // Bidirectional relationship with different fetch type than in Product
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // No timestamps like in Product

    // Helper method to add product - inconsistent with Product entity design
    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    // Helper method to remove product - inconsistent with Product entity design
    public void removeProduct(Product product) {
        products.remove(product);
        product.setCategory(null);
    }
}