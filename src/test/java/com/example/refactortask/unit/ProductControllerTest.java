package com.example.refactortask.unit;

import com.example.refactortask.controller.ProductController;
import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void should_getAllProducts_returnEmptyList_whenNoProductsExist() throws Exception {
        // Given
        List<ProductDTO> emptyList = new ArrayList<>();
        when(productService.getAllProducts(anyBoolean())).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(productService).getAllProducts(false);
    }

    @Test
    public void should_getAllProducts_returnProductList_whenProductsExist() throws Exception {
        // Given
        List<ProductDTO> products = List.of(
                ProductDTO.builder()
                        .id(1L)
                        .productName("Laptop")
                        .description("A powerful laptop")
                        .productPrice(new BigDecimal("999.99"))
                        .stock_quantity(10)
                        .categoryId(1L)
                        .isInStock(true)
                        .build(),
                ProductDTO.builder()
                        .id(2L)
                        .productName("Smartphone")
                        .description("A new smartphone")
                        .productPrice(new BigDecimal("599.99"))
                        .stock_quantity(20)
                        .categoryId(1L)
                        .isInStock(true)
                        .build()
        );
        when(productService.getAllProducts(anyBoolean())).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productName").value("Laptop"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productName").value("Smartphone"));

        verify(productService).getAllProducts(false);
    }

    @Test
    public void should_getProductById_returnProduct_whenProductExists() throws Exception {
        // Given
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .productName("Laptop")
                .description("A powerful laptop")
                .productPrice(new BigDecimal("999.99"))
                .stock_quantity(10)
                .categoryId(1L)
                .isInStock(true)
                .build();
        when(productService.getProductById(anyLong())).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.description").value("A powerful laptop"))
                .andExpect(jsonPath("$.productPrice").value(999.99))
                .andExpect(jsonPath("$.stock_quantity").value(10))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.isInStock").value(true));

        verify(productService).getProductById(1L);
    }

    @Test
    public void should_fail_getProductById_callServiceMethod_whenProductDoesNotExist() throws Exception {
        // Given
        when(productService.getProductById(anyLong())).thenThrow(new RuntimeException("Product not found"));

        try {
            // When
            mockMvc.perform(get("/api/products/999"));
        } catch (Exception e) {
            // Expected exception, ignore
        }

        // Then
        verify(productService).getProductById(999L);
    }

    @Test
    public void should_createProduct_returnCreatedProduct_whenValidInput() throws Exception {
        // Given
        ProductDTO inputProduct = ProductDTO.builder()
                .productName("Smartphone")
                .description("A new smartphone")
                .productPrice(new BigDecimal("599.99"))
                .stock_quantity(20)
                .categoryId(1L)
                .isInStock(true)
                .build();

        ProductDTO createdProduct = ProductDTO.builder()
                .id(1L)
                .productName("Smartphone")
                .description("A new smartphone")
                .productPrice(new BigDecimal("599.99"))
                .stock_quantity(20)
                .categoryId(1L)
                .isInStock(true)
                .build();

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Smartphone"))
                .andExpect(jsonPath("$.description").value("A new smartphone"))
                .andExpect(jsonPath("$.productPrice").value(599.99))
                .andExpect(jsonPath("$.stock_quantity").value(20))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.isInStock").value(true));

        verify(productService).createProduct(any(ProductDTO.class));
    }

    @Test
    public void should_createCategory_returnCreatedCategory_whenValidInput() throws Exception {
        // Given
        CategoryDTO inputCategory = new CategoryDTO(
                null,
                "Books",
                "Book collection",
                null
        );

        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Books");
        category.setDescription("Book collection");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When & Then
        mockMvc.perform(post("/api/products/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCategory)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Books"))
                .andExpect(jsonPath("$.description").value("Book collection"));

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    public void should_syncWithFakeApi_returnNoContent() throws Exception {
        // Given
        when(productService.syncWithFakeApi()).thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(post("/api/products/sync-with-fake-api"))
                .andExpect(status().isNoContent());

        verify(productService).syncWithFakeApi();
    }
}
