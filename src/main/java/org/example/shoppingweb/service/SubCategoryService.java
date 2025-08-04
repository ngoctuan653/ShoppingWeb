package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Subcategory;
import org.example.shoppingweb.repository.CategoryRepository;
import org.example.shoppingweb.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class SubCategoryService {
    @Autowired
    private SubCategoryRepository subCategoryRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public Subcategory findById(Integer id) {
        return subCategoryRepository.findById(id).orElseThrow(() ->
                new RuntimeException("SubCategory not found with ID: " + id));
    }

    public Subcategory getSubCategoryById(Integer id) {
        return subCategoryRepository.findById(id).orElse(null);
    }

    public Subcategory save(Subcategory subCategory) {
        if (subCategory.getId() == null) {
            subCategory.setCreatedAt(Instant.now());
        } else {
            Subcategory existing = subCategoryRepository.findById(subCategory.getId()).orElse(null);
            if (existing != null) {
                subCategory.setCreatedAt(existing.getCreatedAt());
            }
        }

        // Ensure category reference is managed
        if (subCategory.getCategory() != null && subCategory.getCategory().getId() != null) {
            categoryRepository.findById(subCategory.getCategory().getId()).ifPresent(subCategory::setCategory);
        }

        subCategory.setUpdatedAt(Instant.now());
        return subCategoryRepository.save(subCategory);
    }

    public void deleteById(Integer id) {
        subCategoryRepository.deleteById(id);
    }
}
