package com.example.refactortask.grpc;

import com.example.refactortask.client.FakeStoreApiClient;
import com.example.refactortask.model.dto.ExternalProductDTO;
import com.example.refactortask.model.dto.ProductDTO;
import com.example.refactortask.service.ProductService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;
    private final FakeStoreApiClient fakeStoreApiClient;
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

        List<ExternalProductDTO> externalProducts = fakeStoreApiClient.getAllProducts();
        log.info("Fetched {} products from external API", externalProducts.size());

        Map<String, ExternalProductDTO> externalProductMap = externalProducts.stream()
                .collect(Collectors.toMap(
                    ep -> ep.getTitle().toLowerCase(),
                    Function.identity(),
                    (existing, replacement) -> existing // In case of duplicate keys, keep the first one
                ));

        products.forEach(productDTO -> {
            ExternalProductDTO matchingProduct = externalProductMap.get(productDTO.getProductName().toLowerCase());
            if (matchingProduct != null) {
                productDTO.setExternalId(matchingProduct.getId().toString());
                productDTO.setRating(matchingProduct.getRating() != null ? matchingProduct.getRating().getRate() : null);
                productDTO.setRatingCount(matchingProduct.getRating() != null ? matchingProduct.getRating().getCount() : null);
                productDTO.setImageUrl(matchingProduct.getImage());
                log.debug("Enriched product {} with external data", productDTO.getProductName());
            } else {
                log.debug("No matching external product found for {}", productDTO.getProductName());
            }
        });

        ListProductsResponse.Builder responseBuilder = ListProductsResponse.newBuilder();
        products.forEach(productDTO -> responseBuilder.addProducts(mapToGrpcResponse(productDTO)));

        responseObserver.onNext(responseBuilder.build());
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
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

        if (productDTO.getExternalId() != null) {
            builder.setExternalId(productDTO.getExternalId());
        }

        if (productDTO.getRating() != null) {
            builder.setRating(productDTO.getRating());
        }

        if (productDTO.getRatingCount() != null) {
            builder.setRatingCount(productDTO.getRatingCount());
        }

        if (productDTO.getImageUrl() != null) {
            builder.setImageUrl(productDTO.getImageUrl());
        }
        return builder.build();
    }
}
