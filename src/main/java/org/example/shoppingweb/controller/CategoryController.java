package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.entity.Category;
import org.example.shoppingweb.entity.Subcategory;
import org.example.shoppingweb.repository.BrandRepository;
import org.example.shoppingweb.repository.CategoryRepository;
import org.example.shoppingweb.repository.SubCategoryRepository;
import org.example.shoppingweb.service.BrandService;
import org.example.shoppingweb.service.CategoryService;
import org.example.shoppingweb.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CategoryController {
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CategoryService categoryService;


    @GetMapping("/admin/category-manage")
    public String categoryManagePage(Model model) {
        List<Category> categories = categoryRepository.findAll();
        List<Subcategory> subCategories = subCategoryRepository.findAll();
        List<Brand> brands = brandRepository.findAll();
        model.addAttribute("activePage", "categories");
        model.addAttribute("categories", categories);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("brands", brands);
        return "category-management";
    }

    @GetMapping("/admin/category/{id}")
    @ResponseBody
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/category")
    @ResponseBody
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        Category saved = categoryService.save(category);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/admin/category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/category/{id}")
    @ResponseBody
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category updatedCategory) {
        return categoryRepository.findById(id).map(existing -> {
            existing.setCategoryName(updatedCategory.getCategoryName());
            existing.setDescription(updatedCategory.getDescription());
            existing.setStatus(updatedCategory.getStatus());
            Category saved = categoryRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

}
