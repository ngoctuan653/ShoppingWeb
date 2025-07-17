package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/index")
    public String showProduct(Model model) {
        List<Product> products = productService.getAllProduct();
        model.addAttribute("products", products);
        return "index";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Product> searchProduct(@RequestParam("keyword") String keyword) {
        return productService.searchProduct(keyword);
    }
}
