package org.example.shoppingweb.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.shoppingweb.DTO.ProductRequest;
import org.example.shoppingweb.DTO.ProductSizeRequest;
import org.example.shoppingweb.DTO.SizeDTO;
import org.example.shoppingweb.DTO.StockUpdateRequest;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.ProductRepository;
import org.example.shoppingweb.repository.ProductSizeRepository;
import org.example.shoppingweb.repository.SizeRepository;
import org.example.shoppingweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private SizeService sizeService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SubCategoryService subCategoryService;
    @Autowired
    private ProductSizeService productSizeService;

    @GetMapping("/shop")
    public String showProduct(Model model) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> availableProducts = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0)
                .collect(Collectors.toList());
        List<Category> categories = productService.getAllCategories();
        List<Brand> brands = productService.getAllBrands();
        List<Subcategory> subcategories = productService.getAllSubcategories();
        model.addAttribute("categories", categories);
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("products", availableProducts);
        model.addAttribute("brands", brands);
        return "shop";
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Product> searchProducts(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Double minPrice,
                                        @RequestParam(required = false) Double maxPrice,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) List<Long> subcategories,
                                        @RequestParam(required = false) List<Long> brands) {
        System.out.println("Keyword: " + keyword);
        System.out.println("Min Price: " + minPrice);
        System.out.println("Max Price: " + maxPrice);
        System.out.println("Categories: " + categories);
        System.out.println("Brands: " + brands);
        return productService.searchProducts(keyword, minPrice, maxPrice, categories, subcategories, brands);
    }


    @GetMapping("/home")
    public String showProductHome(Model model) {
        List<Product> products = productService.getAllActiveProducts();
        model.addAttribute("products", products);
        return "Home";
    }

    @GetMapping("/products")
    public String showProducts(Model model) {
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("subcategories", productService.getAllSubcategories()); // <- thêm dòng này
        model.addAttribute("sizes", sizeRepository.findAll());

        Product p = new Product();
        p.setSubcategory(new Subcategory());
        p.getSubcategory().setCategory(new Category());
        p.setBrand(new Brand());
        model.addAttribute("product", p);

        return "product-managements";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        if (!imageFile.isEmpty()) {
            product.setImage(imageFile.getBytes());
        }
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable("id") Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("products", productService.getAllProduct());
        return "product-managements";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute Product product,
                                @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        Product existing = productService.getProductById(product.getId());

        if (!imageFile.isEmpty()) {
            product.setImage(imageFile.getBytes());
        } else {
            product.setImage(existing.getImage());
        }

        productService.updateProduct(product);
        return "redirect:/products";
    }

    @PostMapping("/products/add")
    public ResponseEntity<?> addProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        Product product = productService.createProduct(request, imageFile);
        return ResponseEntity.ok(product);
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


    @PutMapping("/{productId}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Integer productId,
            @RequestBody StockUpdateRequest request) {

        Product updated = productService.updateStockQuantity(productId, request.getStockQuantity());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/products/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable("id") Integer id) {
        Product product = productService.getProductById(id);
        if (product == null || product.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(product.getImage(), headers, HttpStatus.OK);
    }

    @PostMapping("/products/delete/{id}")
    public String softDeleteProduct(@PathVariable("id") Integer id) {
        productService.updateStatus(id, "Inactive");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}")
    public String getProductDetail(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product-detail"; // -> product-detail.html
    }

//    @GetMapping("/{productId}/sizes")
//    @ResponseBody
//    public ResponseEntity<List<SizeDTO>> getSizesByProduct(@PathVariable Integer productId) {
//        List<Size> sizes = productService.getSizesByProductId(productId);
//
//        List<SizeDTO> result = sizes.stream()
//                .map(size -> new SizeDTO(size.getId(), size.getSizeLabel()))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(result);
//    }

    @GetMapping("/{id}/sizes")
    public ResponseEntity<List<SizeDTO>> getProductSizes(@PathVariable Integer id) {
        List<SizeDTO> sizes = productService.getSizesByProductId(id);
        return ResponseEntity.ok(sizes);
    }

}
