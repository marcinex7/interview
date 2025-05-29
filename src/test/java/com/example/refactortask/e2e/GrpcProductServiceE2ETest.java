package com.example.refactortask.e2e;

import com.example.refactortask.grpc.CreateProductRequest;
import com.example.refactortask.grpc.ListProductsRequest;
import com.example.refactortask.grpc.ListProductsResponse;
import com.example.refactortask.grpc.ProductRequest;
import com.example.refactortask.grpc.ProductResponse;
import com.example.refactortask.grpc.ProductServiceGrpc;
import com.example.refactortask.model.entity.Category;
import com.example.refactortask.model.entity.Product;
import com.example.refactortask.repository.CategoryRepository;
import com.example.refactortask.repository.ProductRepository;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GrpcProductServiceE2ETest {

    @Value("${grpc.server.in-process-name:interview-grpc-server}")
    private String inProcessServerName;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductServiceGrpc.ProductServiceBlockingStub blockingStub;
    private ManagedChannel channel;

    @BeforeEach
    public void setup() {
        // Clean up the database before each test
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Set up the gRPC channel
        channel = InProcessChannelBuilder.forName(inProcessServerName)
                .usePlaintext()
                .build();
        blockingStub = ProductServiceGrpc.newBlockingStub(channel);
    }

    @Test
    public void should_getProduct_returnProduct_whenProductExists() {
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
        ProductRequest request = ProductRequest.newBuilder()
                .setId(savedProduct.getId())
                .build();
        ProductResponse response = blockingStub.getProduct(request);

        // Then
        assertNotNull(response);
        assertEquals(savedProduct.getId(), response.getId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(999.99, response.getProductPrice(), 0.01);
        assertEquals(10, response.getStockQuantity());
        assertTrue(response.getIsInStock());
    }

    @Test
    public void should_fail_getProduct_throwException_whenProductDoesNotExist() {
        // Given
        ProductRequest request = ProductRequest.newBuilder()
                .setId(999L)
                .build();

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            blockingStub.getProduct(request);
        });
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Product not found"));
    }

    @Test
    public void should_listProducts_returnEmptyList_whenNoProductsExist() {
        // When
        ListProductsRequest request = ListProductsRequest.newBuilder().build();
        ListProductsResponse response = blockingStub.listProducts(request);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getProductsCount());
    }

    @Test
    public void should_listProducts_returnProductList_whenProductsExist() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        Category savedCategory = categoryRepository.save(category);

        Product product1 = Product.builder()
                .productName("Laptop")
                .description("A powerful laptop")
                .productPrice(new BigDecimal("999.99"))
                .stock_quantity(10)
                .category(savedCategory)
                .isInStock(true)
                .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
                .productName("Smartphone")
                .description("A new smartphone")
                .productPrice(new BigDecimal("599.99"))
                .stock_quantity(20)
                .category(savedCategory)
                .isInStock(true)
                .build();
        productRepository.save(product2);

        // When
        ListProductsRequest request = ListProductsRequest.newBuilder().build();
        ListProductsResponse response = blockingStub.listProducts(request);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getProductsCount());

        // This test might fail due to the intentional flaw in the getAllProducts method
        // The method tries to remove items from the stream while iterating, which is not allowed
    }

    @Test
    public void should_createProduct_returnCreatedProduct_whenValidInput() {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        Category savedCategory = categoryRepository.save(category);

        CreateProductRequest request = CreateProductRequest.newBuilder()
                .setProductName("Tablet")
                .setDescription("A new tablet")
                .setProductPrice(399.99)
                .setStockQuantity(15)
                .setCategoryId(savedCategory.getCategoryId())
                .build();

        // When
        ProductResponse response = blockingStub.createProduct(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Tablet", response.getProductName());
        assertEquals(399.99, response.getProductPrice(), 0.01);
        assertEquals(15, response.getStockQuantity());
        assertTrue(response.getIsInStock());
        assertEquals(savedCategory.getCategoryId(), response.getCategoryId());
    }
}
