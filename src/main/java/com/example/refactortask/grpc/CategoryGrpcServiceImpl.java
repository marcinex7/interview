package com.example.refactortask.grpc;

import com.example.refactortask.model.dto.CategoryDTO;
import com.example.refactortask.service.CategoryService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Using @Component instead of @Service (inconsistent with ProductServiceImpl)
@Component
public class CategoryGrpcServiceImpl extends CategoryGrpcServiceGrpc.CategoryGrpcServiceImplBase {

    // Field injection instead of constructor injection (inconsistent with ProductServiceImpl)
    @Autowired
    private CategoryService categoryService;

    @Override
    public void fetchCategory(CategoryProto.FetchCategoryRequest request, 
                             StreamObserver<CategoryProto.CategoryData> responseObserver) {
        try {
            // Different approach to get data (using try-catch instead of if-else)
            CategoryDTO categoryDTO = categoryService.getCategoryById(request.getCategoryId());

            // Manual mapping (inconsistent with ProductServiceImpl's helper method)
            CategoryProto.CategoryData response = CategoryProto.CategoryData.newBuilder()
                    .setCategoryId(categoryDTO.id())
                    .setName(categoryDTO.name())
                    .setDescription(categoryDTO.description() != null ? categoryDTO.description() : "")
                    .addAllProductIds(categoryDTO.productIds() != null ? categoryDTO.productIds() : Collections.emptyList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Different error handling approach
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("Category not found: " + e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void getAllCategories(CategoryProto.Empty request, 
                                StreamObserver<CategoryProto.CategoriesList> responseObserver) {
        // Different approach to get all data
        List<CategoryDTO> categories = categoryService.getAllCategories();

        List<CategoryProto.CategoryData> categoryDataList = new ArrayList<>();
        for (CategoryDTO categoryDTO : categories) {
            // Using for loop instead of forEach (inconsistent with ProductServiceImpl)
            CategoryProto.CategoryData categoryData = CategoryProto.CategoryData.newBuilder()
                    .setCategoryId(categoryDTO.id())
                    .setName(categoryDTO.name())
                    .setDescription(categoryDTO.description() != null ? categoryDTO.description() : "")
                    .addAllProductIds(categoryDTO.productIds() != null ? categoryDTO.productIds() : Collections.emptyList())
                    .build();
            categoryDataList.add(categoryData);
        }

        CategoryProto.CategoriesList response = CategoryProto.CategoriesList.newBuilder()
                .addAllCategories(categoryDataList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addCategory(CategoryProto.AddCategoryRequest request, 
                           StreamObserver<CategoryProto.CategoryData> responseObserver) {
        // Creating DTO directly instead of using builder (inconsistent with ProductServiceImpl)
        CategoryDTO categoryDTO = new CategoryDTO(
                null, 
                request.getName(), 
                request.getDescription(),
                Collections.emptyList()
        );

        try {
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);

            CategoryProto.CategoryData response = CategoryProto.CategoryData.newBuilder()
                    .setCategoryId(createdCategory.id())
                    .setName(createdCategory.name())
                    .setDescription(createdCategory.description() != null ? createdCategory.description() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to create category: " + e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void modifyCategory(CategoryProto.ModifyCategoryRequest request, 
                              StreamObserver<CategoryProto.CategoryData> responseObserver) {
        // Creating DTO directly instead of using builder (inconsistent with ProductServiceImpl)
        CategoryDTO categoryDTO = new CategoryDTO(
                request.getCategoryId(), 
                request.getName(), 
                request.getDescription(),
                Collections.emptyList()
        );

        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(request.getCategoryId(), categoryDTO);

            CategoryProto.CategoryData response = CategoryProto.CategoryData.newBuilder()
                    .setCategoryId(updatedCategory.id())
                    .setName(updatedCategory.name())
                    .setDescription(updatedCategory.description() != null ? updatedCategory.description() : "")
                    .addAllProductIds(updatedCategory.productIds() != null ? updatedCategory.productIds() : Collections.emptyList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to update category: " + e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void removeCategory(CategoryProto.RemoveCategoryRequest request, 
                              StreamObserver<CategoryProto.RemoveResponse> responseObserver) {
        try {
            categoryService.deleteCategory(request.getCategoryId());

            // Different response structure than ProductServiceImpl
            CategoryProto.RemoveResponse response = CategoryProto.RemoveResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Different error handling approach
            CategoryProto.RemoveResponse response = CategoryProto.RemoveResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Failed to delete category: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void findCategoryByName(CategoryProto.FindByNameRequest request, 
                                  StreamObserver<CategoryProto.CategoryData> responseObserver) {
        try {
            CategoryDTO categoryDTO = categoryService.findByName(request.getName());

            CategoryProto.CategoryData response = CategoryProto.CategoryData.newBuilder()
                    .setCategoryId(categoryDTO.id())
                    .setName(categoryDTO.name())
                    .setDescription(categoryDTO.description() != null ? categoryDTO.description() : "")
                    .addAllProductIds(categoryDTO.productIds() != null ? categoryDTO.productIds() : Collections.emptyList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("Category not found by name: " + e.getMessage())
                            .asRuntimeException());
        }
    }
}
