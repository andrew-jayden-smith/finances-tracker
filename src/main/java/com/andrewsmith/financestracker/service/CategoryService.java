package com.andrewsmith.financestracker.service;

import com.andrewsmith.financestracker.model.Category;
import com.andrewsmith.financestracker.model.User;
import com.andrewsmith.financestracker.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Create a new category
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Find exact category by name option
    public Optional<Category> findCategoryByName(String name) {
        return Optional.ofNullable(categoryRepository.findCategoriesByName(name));
    }
    // Check if category exists
    public boolean categoryExists(Category category, String name) {
        return categoryRepository.existsByName(name);
    }
    // Search by category autofill name
    public List<Category> searchCategories(String name) {
        return categoryRepository.findCategoriesByNameIgnoringCase(name);
    }
    // List all categories saved
    public List<Category> findAllCategories() {
        return (List<Category>) categoryRepository.findAll();
    }
    // Save new or update category
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }
    // Delete a current category
    public void deleteCategory(Category category) {
        categoryRepository.delete(category);
    }
}
