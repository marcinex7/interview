package com.example.refactortask.controller;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Inconsistent base path compared to ProductController
@RequestMapping("/api/categories")
public class CategoryController {

    // Field injection instead of constructor injection
    @Autowired
    private CategoryService categoryService;

    // Not using ResponseEntity (inconsistent with ProductController)
    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryDTO getCategoryById(@PathVariable("id") Long categoryId) {
        return categoryService.getCategoryById(categoryId);
    }

    // Using @ResponseStatus instead of ResponseEntity
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO createCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.createCategory(categoryDTO);
    }

    // Using PATCH instead of PUT for updates
    @PatchMapping("/{id}")
    public CategoryDTO updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        return categoryService.updateCategory(id, categoryDTO);
    }

    // Using different response status approach
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

    // Additional endpoint with inconsistent naming compared to ProductController
    @GetMapping("/by-name/{name}")
    public CategoryDTO getCategoryByName(@PathVariable String name) {
        return categoryService.findByName(name);
    }
}