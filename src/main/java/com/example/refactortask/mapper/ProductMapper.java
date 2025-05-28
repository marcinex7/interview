package com.example.refactortask.mapper;

import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.categoryId", target = "categoryId")
    ProductDTO toDto(Product product);

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateProductFromDto(ProductDTO productDTO, @MappingTarget Product product);
}