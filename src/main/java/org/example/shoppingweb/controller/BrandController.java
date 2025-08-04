package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.repository.BrandRepository;
import org.example.shoppingweb.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class BrandController {
    @Autowired
    private BrandService brandService;
    @Autowired
    private BrandRepository brandRepository;

    @GetMapping("/admin/brand/{id}")
    @ResponseBody
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        return brandRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/brand")
    @ResponseBody
    public ResponseEntity<Brand> addBrand(@RequestBody Brand brand) {
        Brand saved = brandService.save(brand);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/admin/brand/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Integer id) {
        brandService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/brand/{id}")
    @ResponseBody
    public ResponseEntity<Brand> updateBrand(@PathVariable Integer id, @RequestBody Brand updatedBrand) {
        return brandRepository.findById(id).map(existing -> {
            existing.setBrandName(updatedBrand.getBrandName());
            existing.setDescription(updatedBrand.getDescription());
            existing.setStatus(updatedBrand.getStatus());
            Brand saved = brandRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

}
