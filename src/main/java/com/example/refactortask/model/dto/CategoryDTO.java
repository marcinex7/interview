package com.example.refactortask.model.dto;

import java.util.List;

public record CategoryDTO(
    Long id,
    String name,
    String description,
    List<Long> productIds
) {
    public static CategoryDTO empty() {
        return new CategoryDTO(null, "", "", List.of());
    }
}