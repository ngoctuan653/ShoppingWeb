package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.entity.Productsize;
import org.example.shoppingweb.repository.ProductSizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSizeService {
    @Autowired
    private ProductSizeRepository productSizeRepository;

    public Productsize save(Productsize ps) {
        return productSizeRepository.save(ps);
    }

    public List<Productsize> findByProduct(Product product) {
        return productSizeRepository.findByProduct(product);
    }
}
