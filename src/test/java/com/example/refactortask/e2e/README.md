# E2E Tests for Refactor Task

This directory contains end-to-end tests for the Refactor Task project. The tests are designed to test the functionality
of the ProductController, ProductService, and gRPC ProductServiceImpl classes.

## Test Classes

### ProductControllerE2ETest

Tests the REST API endpoints provided by the ProductController:

- GET /api/products - to get all products
- GET /api/products/{id} - to get a product by ID
- POST /api/products - to create a new product
- POST /api/products/category - to create a new category
- POST /api/products/sync-with-fake-api - to sync with the fake API

### ProductServiceE2ETest

Tests the service methods provided by the ProductService:

- getAllProducts - to get all products
- getProductById - to get a product by ID
- createProduct - to create a new product
- syncWithFakeApi - to sync with the fake API

### GrpcProductServiceE2ETest

Tests the gRPC service methods provided by the ProductServiceImpl:

- getProduct - to get a product by ID
- listProducts - to get all products
- createProduct - to create a new product

## Intentional Flaws

During the implementation of the tests, several intentional flaws were identified in the code:

1. In the `getAllProducts` method of the ProductService, there's an attempt to remove items from a stream while
   iterating, which is not allowed in Java. This causes a `ConcurrentModificationException` when trying to filter out
   products with zero stock.

2. The `getProductById` method in the ProductService uses `productRepository.getById(id)` which might throw an exception
   if the product doesn't exist, instead of returning an Optional or null.

3. The URL in the ProductControllerE2ETest has a double "api" prefix (`http://localhost:" + port + "/api/api/products`),
   which might cause the tests to fail if the context path is already set to "/api" in the application.properties.

4. The tests might fail due to missing configuration for the test profile, as the tests are annotated with
   `@ActiveProfiles("test")` but there might not be a corresponding application-test.properties file.

These flaws are intentional as mentioned in the issue description: "there are intentional flaws made in some methods, so
some of the tests might not work - and that's fine!"