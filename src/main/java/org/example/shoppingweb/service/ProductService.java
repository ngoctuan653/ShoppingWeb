package org.example.shoppingweb.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.example.shoppingweb.DTO.ProductRequest;
import org.example.shoppingweb.DTO.ProductSizeRequest;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private SizeRepository sizeRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Subcategory> getAllSubcategories() {
        return subCategoryRepository.findAll();
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

    public List<Product> searchProducts(String keyword,
                                        Double minPrice,
                                        Double maxPrice,
                                        List<Long> categories,
                                        List<Long> subcategories,
                                        List<Long> brands) {
        return productRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), "Active"));

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(minPrice)));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(maxPrice)));
            }

            // Lọc theo category hoặc subcategory
            if ((categories != null && !categories.isEmpty()) || (subcategories != null && !subcategories.isEmpty())) {
                List<Predicate> orCategorySub = new ArrayList<>();
                if (categories != null && !categories.isEmpty()) {
                    orCategorySub.add(root.get("subcategory").get("category").get("id").in(categories));
                }
                if (subcategories != null && !subcategories.isEmpty()) {
                    orCategorySub.add(root.get("subcategory").get("id").in(subcategories));
                }
                predicates.add(cb.or(orCategorySub.toArray(new Predicate[0])));
            }

            if (brands != null && !brands.isEmpty()) {
                predicates.add(root.get("subcategory").get("brand").get("id").in(brands));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });

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
        return (product != null && product.getSubcategory() != null)
                ? product.getSubcategory().getCategory()
                : null;
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
        existing.setSubcategory(updatedProduct.getSubcategory());
        existing.setBrand(updatedProduct.getBrand());
        existing.setStatus(updatedProduct.getStatus());
        existing.setImage(updatedProduct.getImage());

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


    public List<Size> getSizesByProductId(Integer productId) {
        List<Productsize> productSizes = productSizeRepository.findByProductId(productId);
        return productSizes.stream()
                .map(Productsize::getSize)
                .collect(Collectors.toList());
    }

    public Product createProduct(ProductRequest req, MultipartFile imageFile) {
        Subcategory subCategory = subCategoryRepository.findById(req.getSubCategoryId()).orElseThrow(() -> new RuntimeException("SubCategory not found"));
        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Brand brand = brandRepository.findById(req.getBrandId()).orElseThrow(() -> new RuntimeException("Brand not found"));

        Product product = new Product();
        product.setProductName(req.getProductName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStockQuantity(0);
        product.setCategory(category);
        product.setSubcategory(subCategory);
        product.setBrand(brand);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        product.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        if(imageFile != null && !imageFile.isEmpty()){
            try {
                product.setImage(imageFile.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Product savedProduct = productRepository.save(product);

        int totalStock = 0;

        if (req.getSizes() != null) {
            for (ProductSizeRequest sizeReq : req.getSizes()) {
                Size size = sizeRepository.findBySizeLabel(sizeReq.getSizeLabel())
                        .orElseGet(() -> {
                            Size newSize = new Size();
                            newSize.setSizeLabel(sizeReq.getSizeLabel());
                            newSize.setDescription("");
                            return sizeRepository.save(newSize);
                        });

                Productsize ps = new Productsize();
                ps.setProduct(savedProduct);
                ps.setSize(size);
                ps.setStockQuantity(sizeReq.getStockQuantity());
                ps.setCreatedAt(Instant.now());
                ps.setUpdatedAt(Instant.now());
                productSizeRepository.save(ps);

                totalStock += sizeReq.getStockQuantity();
            }
        }

        savedProduct.setStockQuantity(totalStock);
        productRepository.save(savedProduct);

        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Integer productId, ProductRequest req,MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Subcategory subCategory = subCategoryRepository.findById(req.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("SubCategory not found"));
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        // Cập nhật thông tin cơ bản
        product.setProductName(req.getProductName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setSubcategory(subCategory);
        product.setCategory(category);
        product.setBrand(brand);
        product.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        product.setUpdatedAt(Instant.now());

        if (image != null && !image.isEmpty()) {
            product.setImage(image.getBytes());
        }


        // Xoá các size cũ
        productSizeRepository.deleteByProduct(product);

        int totalStock = 0;
        if (req.getSizes() != null) {
            for (ProductSizeRequest sizeReq : req.getSizes()) {
                Size size = sizeRepository.findBySizeLabel(sizeReq.getSizeLabel())
                        .orElseGet(() -> {
                            Size newSize = new Size();
                            newSize.setSizeLabel(sizeReq.getSizeLabel());
                            newSize.setDescription("");
                            return sizeRepository.save(newSize);
                        });

                Productsize ps = new Productsize();
                ps.setProduct(product);
                ps.setSize(size);
                ps.setStockQuantity(sizeReq.getStockQuantity());
                ps.setCreatedAt(Instant.now());
                ps.setUpdatedAt(Instant.now());
                productSizeRepository.save(ps);

                totalStock += sizeReq.getStockQuantity();
            }
        }

        product.setStockQuantity(totalStock);
        return productRepository.save(product);
    }





    public Product updateStockQuantity(Integer productId, int stockQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }
}
