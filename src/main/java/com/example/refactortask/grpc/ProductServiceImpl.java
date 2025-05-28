package com.example.refactortask.grpc;

import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.service.ProductService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            ProductDTO productDTO = productService.getProductById(request.getId());
            responseObserver.onNext(mapToGrpcResponse(productDTO));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Product not found with id: " + request.getId())
                    .asRuntimeException());
        }
    }

    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ListProductsResponse> responseObserver) {
        List<ProductDTO> products = productService.getAllProducts();

        ListProductsResponse.Builder responseBuilder = ListProductsResponse.newBuilder();
        products.forEach(productDTO -> responseBuilder.addProducts(mapToGrpcResponse(productDTO)));

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        // Manual mapping from gRPC request to DTO (inconsistent with other mappers)
        ProductDTO productDTO = ProductDTO.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .productPrice(BigDecimal.valueOf(request.getProductPrice()))
                .stock_quantity(request.getStockQuantity())
                .categoryId(request.getCategoryId())
                .build();

        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            responseObserver.onNext(mapToGrpcResponse(createdProduct));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to create product: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // Helper method to map ProductDTO to gRPC ProductResponse
    private ProductResponse mapToGrpcResponse(ProductDTO productDTO) {
        ProductResponse.Builder builder = ProductResponse.newBuilder()
                .setId(productDTO.getId())
                .setProductName(productDTO.getProductName())
                .setProductPrice(productDTO.getProductPrice().doubleValue())
                .setStockQuantity(productDTO.getStock_quantity())
                .setIsInStock(productDTO.getIsInStock());

        if (productDTO.getDescription() != null) {
            builder.setDescription(productDTO.getDescription());
        }

        if (productDTO.getCategoryId() != null) {
            builder.setCategoryId(productDTO.getCategoryId());
        }

        if (productDTO.getCreatedAt() != null) {
            builder.setCreatedAt(productDTO.getCreatedAt().format(dateFormatter));
        }

        if (productDTO.getUpdatedAt() != null) {
            builder.setUpdatedAt(productDTO.getUpdatedAt().format(dateFormatter));
        }

        return builder.build();
    }
}
