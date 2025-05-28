package com.example.refactortask.repository;

import com.example.refactortask.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    public Product getById(Long id);

}