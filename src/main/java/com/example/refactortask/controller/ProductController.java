package com.example.refactortask.controller;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    // Using ResponseEntity for all endpoints
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts(true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setCategoryId(categoryDTO.id());
        category.setName(categoryDTO.name());
        category.setDescription(categoryDTO.description());

        Category createdCategory = categoryRepository.save(category);

        CategoryDTO response = new CategoryDTO(
                createdCategory.getCategoryId(),
                createdCategory.getName(),
                createdCategory.getDescription(),
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Syncs product data with the external Fake Store API
     * This endpoint triggers the synchronization of product data with the external API
     * @return ResponseEntity with no content
     */
    @PostMapping("/sync-with-fake-api")
    public ResponseEntity<Void> syncWithFakeApi() {
        // Call the syncWithFakeApi method but don't wait for it to complete
        productService.syncWithFakeApi();
        return ResponseEntity.noContent().build();
    }

}
