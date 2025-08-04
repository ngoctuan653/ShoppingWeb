package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.entity.Category;
import org.example.shoppingweb.entity.Subcategory;
import org.example.shoppingweb.repository.BrandRepository;
import org.example.shoppingweb.repository.CategoryRepository;
import org.example.shoppingweb.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class CategoryController {
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;


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

    @GetMapping("/admin/subcategory/{id}")
    @ResponseBody
    public ResponseEntity<Subcategory> getSubCategoryById(@PathVariable Integer id) {
        return subCategoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/brand/{id}")
    @ResponseBody
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        return brandRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
