package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Discount;
import org.example.shoppingweb.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DiscountController {

    @Autowired
    private DiscountRepository discountRepository;

    @GetMapping("/discount-manage")
    public String discountManagePage(Model model){
        List<Discount> discounts = discountRepository.findAll();
        model.addAttribute("discounts", discounts);
        return "discount-managements";
    }

    @GetMapping("/api/discounts/validate")
    @ResponseBody
    public ResponseEntity<?> validateDiscount(@RequestParam String code) {
        return discountRepository.findByCodeIgnoreCase(code.trim())
                .map(discount -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("percentage", discount.getDiscountPercentage());
                    response.put("description", discount.getDescription());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Invalid discount code");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });

    }
}
