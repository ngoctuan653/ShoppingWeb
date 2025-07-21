package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Subcategory;
import org.example.shoppingweb.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubCategoryService {
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    public Subcategory findById(Integer id) {
        return subCategoryRepository.findById(id).orElseThrow(() ->
                new RuntimeException("SubCategory not found with ID: " + id));
    }

    public Subcategory getSubCategoryById(Integer id) {
        return subCategoryRepository.findById(id).orElse(null);
    }
}
