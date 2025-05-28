package com.example.refactortask.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Using Lombok for Product DTOs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String productName;
    private String description;
    private BigDecimal productPrice; // Inconsistent naming (should be price)
    private Integer stock_quantity; // Inconsistent naming (should be stockQuantity)
    private Long categoryId; // Only storing the ID, not the full category
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isInStock;

    // Additional fields from external API
    private String externalId;
    private Double rating;
    private Integer ratingCount;
    private String imageUrl;
}
