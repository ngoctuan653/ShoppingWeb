package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Category;
import org.example.shoppingweb.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        categoryRepository.findAll().forEach(categories::add);
        return categories;
    }

    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }


    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Category not found with ID: " + id));
    }

    public Category save(Category category) {
        if (category.getId() == null) {
            category.setCreatedAt(Instant.now());
        } else {
            Category existing = categoryRepository.findById(category.getId()).orElse(null);
            if (existing != null) {
                category.setCreatedAt(existing.getCreatedAt());
            }
        }
        category.setUpdatedAt(Instant.now());
        return categoryRepository.save(category);
    }

    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }
}
