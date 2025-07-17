package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.entity.Category;
import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.repository.BrandRepository;
import org.example.shoppingweb.repository.CategoryRepository;
import org.example.shoppingweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatus("Active");
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public void saveProduct(Product product) {
        if (product.getStatus() == null || product.getStatus().isBlank()) {
            product.setStatus("Active");
        }
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }


    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    public Category getCategoryByProductId(Integer productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getCategory() : null;
    }

    public Brand getBrandByProductId(Integer productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getBrand() : null;
    }

    public void updateProduct(Product updatedProduct) {
        Product existing = getProductById(updatedProduct.getId());

        existing.setProductName(updatedProduct.getProductName());
        existing.setPrice(updatedProduct.getPrice());
        existing.setDescription(updatedProduct.getDescription());
        existing.setStockQuantity(updatedProduct.getStockQuantity());
        existing.setCategory(updatedProduct.getCategory());
        existing.setBrand(updatedProduct.getBrand());
        existing.setStatus(updatedProduct.getStatus());
        existing.setImage(updatedProduct.getImage());
        existing.setStatus("Active");

        productRepository.save(existing);
    }


    public void updateStatus(Integer id, String status) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setStatus(status);
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);
        }
    }

    public Product findById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

}
