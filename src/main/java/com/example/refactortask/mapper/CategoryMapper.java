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
        modelMapper.typeMap(Category.class, CategoryDTO.class)
            .setConverter(context -> {
                Category source = context.getSource();
                List<Long> productIds = source.getProducts().stream()
                    .map(product -> product.getId())
                    .collect(Collectors.toList());
                return new CategoryDTO(
                    source.getCategoryId(),
                    source.getName(),
                    source.getDescription(),
                    productIds
                );
            });

        modelMapper.typeMap(CategoryDTO.class, Category.class)
            .setConverter(context -> {
                CategoryDTO source = context.getSource();
                Category category = new Category();
                category.setCategoryId(source.id());
                category.setName(source.name());
                category.setDescription(source.description());
                return category;
            });
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
