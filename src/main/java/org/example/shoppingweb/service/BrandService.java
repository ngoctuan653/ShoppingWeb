package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        brandRepository.findAll().forEach(brands::add);
        return brands;
    }

    public Brand findById(Integer id) {
        return brandRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Brand not found with ID: " + id));
    }

    public Brand save(Brand brand) {
        if (brand.getId() == null) {
            brand.setCreatedAt(Instant.now());
        } else {
            Brand existing = brandRepository.findById(brand.getId()).orElse(null);
            if (existing != null) {
                brand.setCreatedAt(existing.getCreatedAt());
            }
        }
        brand.setUpdatedAt(Instant.now());
        return brandRepository.save(brand);
    }

    public void deleteById(Integer id) {
        brandRepository.deleteById(id);
    }

}
