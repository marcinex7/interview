package com.example.refactortask.mapper;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.model.entity.Category;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// Using ModelMapper for Category mapping (inconsistent with Product using MapStruct)
@Component
public class CategoryMapper {

    private final ModelMapper modelMapper;

    public CategoryMapper() {
        this.modelMapper = new ModelMapper();
        
        // Configure ModelMapper to map between Category and CategoryDTO
        modelMapper.createTypeMap(Category.class, CategoryDTO.class)
            .addMapping(Category::getCategoryId, CategoryDTO::id)
            .addMapping(src -> src.getProducts().stream()
                .map(product -> product.getId())
                .collect(Collectors.toList()), CategoryDTO::productIds);
            
        modelMapper.createTypeMap(CategoryDTO.class, Category.class)
            .addMapping(CategoryDTO::id, Category::setCategoryId);
    }

    public CategoryDTO toDto(Category category) {
        if (category == null) {
            return CategoryDTO.empty();
        }
        return modelMapper.map(category, CategoryDTO.class);
    }

    public Category toEntity(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            return null;
        }
        return modelMapper.map(categoryDTO, Category.class);
    }

    public List<CategoryDTO> toDtoList(List<Category> categories) {
        return categories.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}