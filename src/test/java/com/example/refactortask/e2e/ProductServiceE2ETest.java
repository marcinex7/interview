package com.example.refactortask.e2e;

import com.example.refactortask.client.FakeStoreApiClient;
import com.example.refactortask.model.dto.ExternalProductDTO;
import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.model.entity.Product;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.repository.ProductRepository;
import com.example.refactortask.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceE2ETest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private FakeStoreApiClient fakeStoreApiClient;

    @BeforeEach
    public void setup() {
        // Clean up the database before each test
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        Mockito.reset(fakeStoreApiClient);
    }

    @Test
    public void should_getAllProducts_returnEmptyList_whenNoProductsExist() {
        // When
        List<ProductDTO> products = productService.getAllProducts(false);

        // Then
        assertNotNull(products);
        assertTrue(products.isEmpty());
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
        List<ProductDTO> products = productService.getAllProducts(false);

        // Then
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getProductName());
    }

    @Test
    public void should_fail_getAllProducts_filterOutProductsWithZeroStock() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        Category savedCategory = categoryRepository.save(category);

        Product product1 = Product.builder()
                .productName("Laptop")
                .description("A powerful laptop")
                .productPrice(new BigDecimal("999.99"))
                .stock_quantity(0)  // Zero stock
                .category(savedCategory)
                .isInStock(false)
                .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
                .productName("Smartphone")
                .description("A new smartphone")
                .productPrice(new BigDecimal("599.99"))
                .stock_quantity(10)
                .category(savedCategory)
                .isInStock(true)
                .build();
        productRepository.save(product2);

        // When
        List<ProductDTO> products = productService.getAllProducts(false);

        // Then
        assertNotNull(products);
        // This test might fail due to the intentional flaw in the getAllProducts method
        // The method tries to remove items from the stream while iterating, which is not allowed
        assertEquals(1, products.size());
        assertEquals("Smartphone", products.get(0).getProductName());
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
        ProductDTO productDTO = productService.getProductById(savedProduct.getId());

        // Then
        assertNotNull(productDTO);
        assertEquals("Laptop", productDTO.getProductName());
        assertEquals(savedProduct.getId(), productDTO.getId());
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
        ProductDTO createdProduct = productService.createProduct(productDTO);

        // Then
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getId());
        assertEquals("Smartphone", createdProduct.getProductName());
        assertEquals(savedCategory.getCategoryId(), createdProduct.getCategoryId());

        // Verify it's in the database
        Optional<Product> dbProduct = productRepository.findById(createdProduct.getId());
        assertTrue(dbProduct.isPresent());
        assertEquals("Smartphone", dbProduct.get().getProductName());
    }

    @Test
    public void should_syncWithFakeApi_updateProductsWithExternalData() throws InterruptedException, ExecutionException, TimeoutException {
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

        List<ExternalProductDTO> externalProducts = new ArrayList<>();
        ExternalProductDTO.Rating rating = new ExternalProductDTO.Rating(4.5, 100);
        ExternalProductDTO externalProduct = new ExternalProductDTO(
                1,
                "Laptop",  // Same name as our product for matching
                1099.99,
                "External laptop description",
                "electronics",
                "http://example.com/laptop.jpg",
                rating
        );
        externalProducts.add(externalProduct);

        when(fakeStoreApiClient.getAllProducts()).thenReturn(externalProducts);

        // When
        CompletableFuture<Void> future = productService.syncWithFakeApi();
        future.get(5, TimeUnit.SECONDS);  // Wait for the async operation to complete

        // Then
        // Get the updated product from the database
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);

        // Get the DTO to check the external fields
        ProductDTO updatedProductDTO = productService.getProductById(savedProduct.getId());
        assertNotNull(updatedProductDTO);
        assertEquals("1", updatedProductDTO.getExternalId());
        assertEquals("http://example.com/laptop.jpg", updatedProductDTO.getImageUrl());
        assertEquals(4.5, updatedProductDTO.getRating());
        assertEquals(100, updatedProductDTO.getRatingCount());
        // The test might fail if the service doesn't properly update the product with external data
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public FakeStoreApiClient fakeStoreApiClient() {
            return Mockito.mock(FakeStoreApiClient.class);
        }
    }
}