package org.example.shoppingweb.controller;


import lombok.AllArgsConstructor;
import org.example.shoppingweb.entity.Subcategory;
import org.example.shoppingweb.repository.SubCategoryRepository;
import org.example.shoppingweb.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class SubCategoryController {

    @Autowired
    private SubCategoryService subCategoryService;
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @GetMapping("/admin/subcategory/{id}")
    @ResponseBody
    public ResponseEntity<Subcategory> getSubCategoryById(@PathVariable Integer id) {
        return subCategoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/subcategory")
    @ResponseBody
    public ResponseEntity<Subcategory> addSubcategory(@RequestBody Subcategory subCategory) {
        Subcategory saved = subCategoryService.save(subCategory);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/admin/subcategory/{id}")
    public ResponseEntity<?> deleteSubcategory(@PathVariable Integer id) {
        subCategoryService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/subcategory/{id}")
    @ResponseBody
    public ResponseEntity<Subcategory> updateSubcategory(@PathVariable Integer id, @RequestBody Subcategory updatedSubcategory) {
        return subCategoryRepository.findById(id).map(existing -> {
            existing.setSubcategoryName(updatedSubcategory.getSubcategoryName());
            existing.setDescription(updatedSubcategory.getDescription());
            existing.setStatus(updatedSubcategory.getStatus());
            existing.setCategory(updatedSubcategory.getCategory());
            existing.setBrand(updatedSubcategory.getBrand()); // Nếu có brand
            Subcategory saved = subCategoryRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

}
