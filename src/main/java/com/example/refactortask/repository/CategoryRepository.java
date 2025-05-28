package com.example.refactortask.repository;

import com.example.refactortask.model.entity.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    Optional<Category> getByName(String name);
    
    boolean existsByName(String name);
}