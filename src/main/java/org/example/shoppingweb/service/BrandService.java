package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
