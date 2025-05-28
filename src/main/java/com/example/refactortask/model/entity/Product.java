package com.example.refactortask.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description", length = 1000)
    private String description;

    // Inconsistent naming (should be price)
    @Column(name = "product_price", nullable = false)
    private BigDecimal productPrice;

    // Inconsistent naming (should be stockQuantity)
    @Column(name = "stock")
    private Integer stock_quantity;

    @ManyToOne(fetch = FetchType.EAGER) // Eager loading is often not optimal
    @JoinColumn(name = "category_id")
    private Category category;

    // Inconsistent date naming
    @Column(name = "created_date")
    private LocalDateTime createdAt;

    @Column(name = "updated_date")
    private LocalDateTime updatedAt;

    // Redundant field that could be derived
    @Column(name = "is_in_stock")
    private Boolean isInStock;

    // Pre-persist hook to set dates and derived fields
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isInStock = stock_quantity > 0;
    }

    // Pre-update hook to update the updatedAt field
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        isInStock = stock_quantity > 0;
    }
}