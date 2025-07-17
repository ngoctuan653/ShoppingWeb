package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.entity.Category;
import org.example.shoppingweb.entity.Product;
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

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/index")
    public String showProduct(Model model) {
        List<Product> products = productService.getAllActiveProducts();
        model.addAttribute("products", products);
        return "index";
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
        Product p = new Product();
        p.setCategory(new Category());
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


}
