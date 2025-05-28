package com.example.refactortask.repository;

import com.example.refactortask.model.entity.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

// Inconsistent with ProductRepository: using CrudRepository instead of JpaRepository
// and not using @Repository annotation
public interface CategoryRepository extends CrudRepository<Category, Long> {

    // Using different naming convention than in ProductRepository
    Optional<Category> getByName(String name);
    
    // Inconsistent method naming (find vs get)
    boolean existsByName(String name);
}