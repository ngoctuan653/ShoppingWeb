package org.example.shoppingweb.controller;


import org.example.shoppingweb.DTO.ProductRequest;
import org.example.shoppingweb.DTO.SizeDTO;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.ProductRepository;
import org.example.shoppingweb.repository.ProductSizeRepository;
import org.example.shoppingweb.repository.ReviewRepository;
import org.example.shoppingweb.repository.SizeRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.example.shoppingweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private SizeRepository sizeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/shop")
    public String showProduct(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> availableProducts = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStatus().equals("Active"))
                .limit(6)
                .collect(Collectors.toList());
        List<Category> categories = productService.getAllCategories();
        List<Brand> brands = productService.getAllBrands();
        List<Subcategory> subcategories = productService.getAllSubcategories();
        model.addAttribute("categories", categories);
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("products", availableProducts);
        model.addAttribute("brands", brands);
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
        }
        return "shop";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> loadProducts(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "6") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.getContent().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStatus().equals("Active"))
                .limit(6)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<Product> searchProducts(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Double minPrice,
                                        @RequestParam(required = false) Double maxPrice,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) List<Long> subcategories,
                                        @RequestParam(required = false) List<Long> brands,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "6") int size) {
        System.out.println("Keyword: " + keyword);
        System.out.println("Min Price: " + minPrice);
        System.out.println("Max Price: " + maxPrice);
        System.out.println("Categories: " + categories);
        System.out.println("Brands: " + brands);
        System.out.println("Page: " + page);
        System.out.println("Size: " + size);
        Pageable pageable = PageRequest.of(page, size);
        return productService.searchProducts(keyword, minPrice, maxPrice, categories, subcategories, brands, pageable);
    }


    @GetMapping("/home")
    public String showProductHome(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> availableProducts = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStatus().equals("Active"))
                .collect(Collectors.toList());
        model.addAttribute("products", availableProducts);

        // Truyền thông tin user cho chat box
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
        }
        return "Home";
    }


    @GetMapping("/admin/product-manage")
    public String showProducts(Model model) {
        List<Product> allProducts = productService.getAllProduct();

        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(p -> "Active".equalsIgnoreCase(p.getStatus()))
                .count();
        long lowStockProducts = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5)
                .count();

        model.addAttribute("products", allProducts);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("subcategories", productService.getAllSubcategories());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("activePage", "products");
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);

        // Dùng cho form Thymeleaf nếu cần
        Product p = new Product();
        p.setSubcategory(new Subcategory());
        p.getSubcategory().setCategory(new Category());
        p.setBrand(new Brand());
        model.addAttribute("product", p);

        return "product-managements";
    }


    @PostMapping("/products/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Integer id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Sản phẩm đã được xóa thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/products/add")
    public ResponseEntity<?> addProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            Product product = productService.createProduct(request, imageFile);
            return ResponseEntity.ok(product);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi xử lý file: " + e.getMessage());
        }
    }


    @PostMapping("/products/update/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            Product updated = productService.updateProduct(id, productRequest, image);
            System.out.println("[DEBUG] ProductRequest: " + productRequest);
            return ResponseEntity.ok(Map.of(
                    "message", "Product updated successfully",
                    "productId", updated.getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/products/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable("id") Integer id) {
        Product product = productService.getProductById(id);

        byte[] imageBytes;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        if (product == null || product.getImage() == null) {
            try {
                InputStream is = getClass().getResourceAsStream("/static/images/default.png");
                if (is == null) {
                    return ResponseEntity.notFound().build(); // fallback vẫn failed
                }
                imageBytes = is.readAllBytes();
                headers.setContentType(MediaType.IMAGE_PNG); // đúng định dạng default
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            imageBytes = product.getImage(); // ảnh thực
        }

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }


    @PostMapping("/products/hide/{id}")
    @ResponseBody
    public ResponseEntity<String> softDeleteProduct(@PathVariable("id") Integer id) {
        try {
            productService.updateStatus(id, "Inactive");
            return ResponseEntity.ok("Sản phẩm đã được ẩn thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi ẩn sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public String getProductDetail(@PathVariable Integer id,
                                   Model model,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);

        boolean inWishlist = false;
        if (userDetails != null) {
            User currentUser = userDetails.getUser();
            inWishlist = wishlistService.existsInWishlist(currentUser.getId(), id);
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
        }
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(id);
        model.addAttribute("inWishlist", inWishlist);
        model.addAttribute("reviews", reviews);

        return "product-detail";
    }


    @GetMapping("/{id}/sizes")
    public ResponseEntity<List<SizeDTO>> getProductSizes(@PathVariable Integer id) {
        List<SizeDTO> sizes = productService.getSizesByProductId(id);
        return ResponseEntity.ok(sizes);
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<ProductRequest> getProductForEdit(@PathVariable Integer id) {
        ProductRequest productRequest = productService.getProductRequestById(id); // convert từ entity
        return ResponseEntity.ok(productRequest);
    }

    @GetMapping("/admin/products/search")
    @ResponseBody
    public ResponseEntity<List<Product>> searchAdminProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer brand,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer subcategory,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String stockLevel // low / medium / high
    ) {
        List<Product> all = productService.getAllProduct();

        List<Product> filtered = all.stream()
                .filter(p -> keyword == null || p.getProductName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(p -> brand == null || (p.getBrand() != null && p.getBrand().getId().equals(brand)))
                .filter(p -> category == null || (p.getCategory() != null && p.getCategory().getId().equals(category)))
                .filter(p -> subcategory == null || (p.getSubcategory() != null && p.getSubcategory().getId().equals(subcategory)))
                .filter(p -> status == null || p.getStatus().equalsIgnoreCase(status))
                .filter(p -> {
                    if (stockLevel == null) return true;
                    int qty = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                    return switch (stockLevel) {
                        case "low" -> qty < 10;
                        case "medium" -> qty >= 10 && qty <= 50;
                        case "high" -> qty > 50;
                        default -> true;
                    };
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

}
