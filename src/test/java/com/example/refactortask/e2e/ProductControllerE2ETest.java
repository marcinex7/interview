package com.example.refactortask.e2e;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.model.entity.Product;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProductControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setup() {
        // Clean up the database before each test
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    public void should_getAllProducts_returnEmptyList_whenNoProductsExist() {
        // When
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                }
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    public void should_getAllProducts_returnProductList_whenProductsExist() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        Category savedCategory = categoryRepository.save(category);

        Product product = Product.builder()
                .productName("Laptop")
                .description("A powerful laptop")
                .productPrice(new BigDecimal("999.99"))
                .stock_quantity(10)
                .category(savedCategory)
                .isInStock(true)
                .build();
        productRepository.save(product);

        // When
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                }
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Laptop", response.getBody().get(0).getProductName());
    }

    @Test
    public void should_getProductById_returnProduct_whenProductExists() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        Category savedCategory = categoryRepository.save(category);

        Product product = Product.builder()
                .productName("Laptop")
                .description("A powerful laptop")
                .productPrice(new BigDecimal("999.99"))
                .stock_quantity(10)
                .category(savedCategory)
                .isInStock(true)
                .build();
        Product savedProduct = productRepository.save(product);

        // When
        ResponseEntity<ProductDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + savedProduct.getId(),
                ProductDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Laptop", response.getBody().getProductName());
        assertEquals(savedProduct.getId(), response.getBody().getId());
    }

    @Test
    public void should_fail_getProductById_returnNotFound_whenProductDoesNotExist() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/999",
                String.class
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void should_createProduct_returnCreatedProduct_whenValidInput() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        Category savedCategory = categoryRepository.save(category);

        ProductDTO productDTO = ProductDTO.builder()
                .productName("Smartphone")
                .description("A new smartphone")
                .productPrice(new BigDecimal("599.99"))
                .stock_quantity(20)
                .categoryId(savedCategory.getCategoryId())
                .build();

        // When
        ResponseEntity<ProductDTO> response = restTemplate.postForEntity(
                getBaseUrl(),
                productDTO,
                ProductDTO.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Smartphone", response.getBody().getProductName());
        assertEquals(savedCategory.getCategoryId(), response.getBody().getCategoryId());
    }

    @Test
    public void should_createCategory_returnCreatedCategory_whenValidInput() {
        // Given
        CategoryDTO categoryDTO = new CategoryDTO(
                null,
                "Books",
                "Book collection",
                null
        );

        // When
        ResponseEntity<CategoryDTO> response = restTemplate.postForEntity(
                getBaseUrl() + "/category",
                categoryDTO,
                CategoryDTO.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().id());
        assertEquals("Books", response.getBody().name());
        assertEquals("Book collection", response.getBody().description());
    }

    @Test
    public void should_syncWithFakeApi_returnNoContent() {
        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
                getBaseUrl() + "/sync-with-fake-api",
                null,
                Void.class
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/api/products";
    }
}