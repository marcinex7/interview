package com.example.refactortask.repository;

import com.example.refactortask.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.productPrice <= :price")
    List<Product> findProductsCheaperThan(@Param("price") BigDecimal price);

    @Query(value = "SELECT * FROM products p WHERE p.stock > 0", nativeQuery = true)
    List<Product> findProductsInStock();

    List<Product> findByCategoryCategoryId(Long categoryId);
}