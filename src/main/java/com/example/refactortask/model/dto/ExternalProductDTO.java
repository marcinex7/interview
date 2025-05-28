package com.example.refactortask.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a product from the external Fake Store API
 * Based on the structure from https://fakestoreapi.com/products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProductDTO {
    private Integer id;
    private String title;
    private Double price;
    private String description;
    private String category;
    private String image;
    private Rating rating;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rating {
        private Double rate;
        private Integer count;
    }
}