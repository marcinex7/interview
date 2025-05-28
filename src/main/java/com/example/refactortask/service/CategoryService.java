package com.example.refactortask.service;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    // Using field injection instead of constructor injection (inconsistent with ProductService)
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    // Not using @Transactional (inconsistent with ProductService)
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        categoryRepository.findAll().forEach(categories::add);
        return categoryMapper.toDtoList(categories);
    }
    
    public CategoryDTO getCategoryById(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        // Using if-else instead of orElseThrow (inconsistent with ProductService)
        if (categoryOpt.isPresent()) {
            return categoryMapper.toDto(categoryOpt.get());
        } else {
            throw new RuntimeException("Category not found with id: " + id);
        }
    }
    
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Checking if category exists by name (unnecessary validation)
        if (categoryRepository.existsByName(categoryDTO.name())) {
            throw new RuntimeException("Category with name " + categoryDTO.name() + " already exists");
        }
        
        Category category = categoryMapper.toEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }
    
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        // Different approach than ProductService
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        
        Category category = categoryMapper.toEntity(categoryDTO);
        // Manually setting the ID (inconsistent with ProductService)
        category.setCategoryId(id);
        
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(updatedCategory);
    }
    
    public void deleteCategory(Long id) {
        // Different approach than ProductService
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            categoryRepository.delete(categoryOpt.get());
        } else {
            throw new RuntimeException("Category not found with id: " + id);
        }
    }
    
    // Additional method with different naming convention
    public CategoryDTO findByName(String name) {
        return categoryRepository.getByName(name)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
    }
}