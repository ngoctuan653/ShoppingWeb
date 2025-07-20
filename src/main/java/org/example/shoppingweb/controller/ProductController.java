package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.SizeDTO;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.ProductSizeRepository;
import org.example.shoppingweb.service.ProductService;
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
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductSizeRepository productsizeRepository;

    @GetMapping("/shop")
    public String showProduct(Model model) {
        List<Product> products = productService.getAllActiveProducts();
        List<Category> categories = productService.getAllCategories();
        List<Brand> brands = productService.getAllBrands();
        List<Subcategory> subcategories = productService.getAllSubcategories();
        model.addAttribute("categories", categories);
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("products", products);
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
        // Lấy dữ liệu sản phẩm cũ từ DB
        Product existing = productService.getProductById(product.getId());

        // Nếu người dùng không chọn ảnh mới, giữ lại ảnh cũ
        if (!imageFile.isEmpty()) {
            product.setImage(imageFile.getBytes());
        } else {
            product.setImage(existing.getImage());
        }

        productService.updateProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/products/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable("id") Integer id) {
        Product product = productService.getProductById(id);
        if (product == null || product.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // hoặc IMAGE_PNG nếu ảnh là PNG
        return new ResponseEntity<>(product.getImage(), headers, HttpStatus.OK);
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        if (!imageFile.isEmpty()) {
            product.setImage(imageFile.getBytes());
        }

        productService.saveProduct(product);
        return "redirect:/products";
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

    @GetMapping("/{productId}/sizes")
    @ResponseBody
    public ResponseEntity<List<SizeDTO>> getSizesByProduct(@PathVariable Integer productId) {
        List<Size> sizes = productService.getSizesByProductId(productId);

        List<SizeDTO> result = sizes.stream()
                .map(size -> new SizeDTO(size.getId(), size.getSizeLabel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
