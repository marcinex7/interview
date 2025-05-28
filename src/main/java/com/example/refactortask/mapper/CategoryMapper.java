package com.example.refactortask.mapper;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.model.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryDTO toDto(Category category) {
        if (category == null) {
            return CategoryDTO.empty();
        }

        List<Long> productIds = category.getProducts().stream()
            .map(product -> product.getId())
            .collect(Collectors.toList());

        return new CategoryDTO(
            category.getCategoryId(),
            category.getName(),
            category.getDescription(),
            productIds
        );
    }

    public Category toEntity(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            return null;
        }

        Category category = new Category();
        category.setCategoryId(categoryDTO.id());
        category.setName(categoryDTO.name());
        category.setDescription(categoryDTO.description());
        return category;
    }

    public List<CategoryDTO> toDtoList(List<Category> categories) {
        return categories.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
