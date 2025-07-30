package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/images/products/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        Product product = productRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(product.getImage());
    }
}