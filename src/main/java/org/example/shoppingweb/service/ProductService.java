package org.example.shoppingweb.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.example.shoppingweb.DTO.ProductRequest;
import org.example.shoppingweb.DTO.ProductSizeRequest;
import org.example.shoppingweb.DTO.SizeDTO;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
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

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Page<Product> searchProducts(String keyword,
                                        Double minPrice,
                                        Double maxPrice,
                                        List<Long> categories,
                                        List<Long> subcategories,
                                        List<Long> brands,
                                        Pageable pageable) {
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
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (subcategories != null && !subcategories.isEmpty()) {
                predicates.add(root.get("subcategory").get("id").in(subcategories));
            }
            if (brands != null && !brands.isEmpty()) {
                predicates.add(root.get("brand").get("id").in(brands));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }


    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
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

    public List<SizeDTO> getSizesByProductId(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return product.getProductSizes().stream()
                .map(size -> new SizeDTO(size.getId(), size.getSize().getSizeLabel(), size.getStockQuantity()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Product createProduct(ProductRequest req, MultipartFile image) throws IOException {
        Product product = new Product();
        product.setProductName(req.getProductName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStatus(req.getStatus());
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Subcategory subcategory = subCategoryRepository.findById(req.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        product.setCategory(category);
        product.setSubcategory(subcategory);
        product.setBrand(brand);

        if (image != null && !image.isEmpty()) {
            product.setImage(image.getBytes());
        }

        // Lưu trước để có ID
        Product savedProduct = productRepository.save(product);

        // Gom các size theo label (loại bỏ khoảng trắng và chuẩn hóa về uppercase)
        Map<String, Integer> sizeQuantityMap = new HashMap<>();
        for (ProductSizeRequest sizeReq : req.getSizes()) {
            String normalizedLabel = sizeReq.getSizeLabel().trim().toUpperCase();
            sizeQuantityMap.put(normalizedLabel,
                    sizeQuantityMap.getOrDefault(normalizedLabel, 0) + sizeReq.getStockQuantity());
        }

        int totalStock = 0;
        List<Productsize> productSizes = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sizeQuantityMap.entrySet()) {
            String sizeLabel = entry.getKey();
            int quantity = entry.getValue();

            Size size = sizeRepository.findBySizeLabel(sizeLabel)
                    .orElseGet(() -> {
                        Size newSize = new Size();
                        newSize.setSizeLabel(sizeLabel);
                        newSize.setDescription("");
                        newSize.setCreatedAt(Instant.now());
                        newSize.setUpdatedAt(Instant.now());
                        return sizeRepository.save(newSize);
                    });

            Productsize ps = new Productsize();
            ps.setProduct(savedProduct);
            ps.setSize(size);
            ps.setStockQuantity(quantity);
            ps.setCreatedAt(Instant.now());
            ps.setUpdatedAt(Instant.now());

            productSizes.add(ps);
            totalStock += quantity;
        }

        productSizeRepository.saveAll(productSizes);
        savedProduct.setStockQuantity(totalStock);

        return productRepository.save(savedProduct);
    }



    @Transactional
    public Product updateProduct(Integer id, ProductRequest productRequest, MultipartFile image) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        product.setProductName(productRequest.getProductName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStatus(productRequest.getStatus());
        product.setUpdatedAt(Instant.now());

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Subcategory subcategory = subCategoryRepository.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));
        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        product.setCategory(category);
        product.setSubcategory(subcategory);
        product.setBrand(brand);

        if (image != null && !image.isEmpty()) {
            product.setImage(image.getBytes());
        }

        // Lấy danh sách size hiện có
        List<Productsize> existingSizes = productSizeRepository.findByProduct(product);
        Map<Integer, Productsize> existingSizeMap = existingSizes.stream()
                .filter(ps -> ps.getSize() != null && ps.getSize().getId() != null)
                .collect(Collectors.toMap(ps -> ps.getSize().getId(), ps -> ps));

        // Gom nhóm theo sizeLabel (chuyển về uppercase để chuẩn hóa)
        Map<String, Integer> mergedSizes = new HashMap<>();
        for (ProductSizeRequest sizeDTO : productRequest.getSizes()) {
            String normalizedLabel = sizeDTO.getSizeLabel().trim().toUpperCase();
            mergedSizes.put(normalizedLabel,
                    mergedSizes.getOrDefault(normalizedLabel, 0) + sizeDTO.getStockQuantity());
        }

        List<Productsize> updatedSizes = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : mergedSizes.entrySet()) {
            String sizeLabel = entry.getKey();
            int stockQuantity = entry.getValue();

            Size size = sizeRepository.findBySizeLabel(sizeLabel)
                    .orElseGet(() -> {
                        Size newSize = new Size();
                        newSize.setSizeLabel(sizeLabel);
                        newSize.setCreatedAt(Instant.now());
                        newSize.setUpdatedAt(Instant.now());
                        return sizeRepository.save(newSize);
                    });

            Productsize existing = existingSizeMap.get(size.getId());

            if (existing != null) {
                // Cập nhật size đã tồn tại
                existing.setStockQuantity(stockQuantity);
                existing.setUpdatedAt(Instant.now());
                updatedSizes.add(existing);
                existingSizeMap.remove(size.getId());
            } else {
                // Tạo size mới
                Productsize newSize = new Productsize();
                newSize.setProduct(product);
                newSize.setSize(size);
                newSize.setStockQuantity(stockQuantity);
                newSize.setCreatedAt(Instant.now());
                newSize.setUpdatedAt(Instant.now());
                updatedSizes.add(newSize);
            }
        }

        // Xoá các size không còn nữa
        for (Productsize ps : existingSizeMap.values()) {
            productSizeRepository.delete(ps);
        }

        // Lưu tất cả size
        productSizeRepository.saveAll(updatedSizes);

        // Tính tổng stock
        int totalStock = updatedSizes.stream()
                .mapToInt(ps -> ps.getStockQuantity() != null ? ps.getStockQuantity() : 0)
                .sum();
        product.setStockQuantity(totalStock);

        return productRepository.save(product);
    }


    public ProductRequest getProductRequestById(Integer id) {
        Product product = getProductById(id);
        if (product == null) return null;

        ProductRequest request = new ProductRequest();
        request.setId(product.getId());
        request.setProductName(product.getProductName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);

        if (product.getCategory() != null) {
            request.setCategoryId(product.getCategory().getId());
        }

        if (product.getSubcategory() != null) {
            request.setSubCategoryId(product.getSubcategory().getId());
        }

        if (product.getBrand() != null) {
            request.setBrandId(product.getBrand().getId());
        }

        request.setStatus(product.getStatus());

        List<ProductSizeRequest> sizeRequests = product.getProductSizes().stream()
                .map(ps -> {
                    ProductSizeRequest psr = new ProductSizeRequest();
                    psr.setId(ps.getId());
                    psr.setSizeLabel(ps.getSize().getSizeLabel());
                    psr.setStockQuantity(ps.getStockQuantity());
                    return psr;
                })
                .toList();

        request.setSizes(sizeRequests);

        return request;
    }
}
