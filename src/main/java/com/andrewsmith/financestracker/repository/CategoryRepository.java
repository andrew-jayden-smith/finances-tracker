package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Category findByName(String name);
    boolean existsByName(String name);
    List<Category> findByNameContainingIgnoreCase(String name);
}
