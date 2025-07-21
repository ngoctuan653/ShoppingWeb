package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Category;
import org.example.shoppingweb.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
