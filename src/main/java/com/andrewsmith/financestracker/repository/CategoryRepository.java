package com.andrewsmith.financestracker.repository;

import com.andrewsmith.financestracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Category findCategoriesByName(String name);
    boolean existsByName(String name);
    List<Category> findCategoriesByNameIgnoringCase(String name);
}
