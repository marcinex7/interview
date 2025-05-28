package com.example.refactortask.model.dto;

import java.util.List;

// Using Java record for Category DTO (inconsistent with Product DTO using Lombok)
public record CategoryDTO(
    Long id, // Inconsistent with entity field name (categoryId)
    String name,
    String description,
    List<Long> productIds // Only storing IDs, not full products
) {
    // Static factory method (inconsistent with Product DTO)
    public static CategoryDTO empty() {
        return new CategoryDTO(null, "", "", List.of());
    }
}