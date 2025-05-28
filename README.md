# Refactor Task Application

This is a sample Spring Boot application that demonstrates a REST API and gRPC service for managing products and categories. The application is intentionally designed with various inconsistencies and flaws for a refactoring exercise.

## Technologies Used

- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- gRPC
- MapStruct and ModelMapper for object mapping
- Lombok
- Gradle (build system)

## Running the Application

1. Clone the repository
2. Build the application: `./gradlew build`
3. Run the application: `./gradlew bootRun`

Note: This project was migrated from Maven to Gradle.

## API Endpoints

### REST API

#### Products

- GET `/api/products` - Get all products
- GET `/api/products/{id}` - Get product by ID
- POST `/api/products` - Create a new product
- PUT `/api/products/{id}` - Update a product
- DELETE `/api/products/{id}` - Delete a product
- GET `/api/products/search?name={name}` - Search products by name
- GET `/api/products/price-less-than/{price}` - Get products cheaper than a price
- GET `/api/products/in-stock` - Get products in stock

#### Categories

- GET `/api/categories` - Get all categories
- GET `/api/categories/{id}` - Get category by ID
- POST `/api/categories` - Create a new category
- PATCH `/api/categories/{id}` - Update a category
- DELETE `/api/categories/{id}` - Delete a category
- GET `/api/categories/by-name/{name}` - Get category by name

### gRPC Services

#### ProductService

- GetProduct - Get a product by ID
- ListProducts - Get all products
- CreateProduct - Create a new product
- UpdateProduct - Update a product
- DeleteProduct - Delete a product

#### CategoryGrpcService

- FetchCategory - Get a category by ID
- GetAllCategories - Get all categories
- AddCategory - Create a new category
- ModifyCategory - Update a category
- RemoveCategory - Delete a category
- FindCategoryByName - Get a category by name

## Database

The application uses an in-memory H2 database. The H2 console is available at `/h2-console` with the following credentials:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`
