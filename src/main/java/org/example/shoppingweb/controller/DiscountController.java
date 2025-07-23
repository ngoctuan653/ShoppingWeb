package org.example.shoppingweb.controller;

import org.example.shoppingweb.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired
    private DiscountRepository discountRepository;

    @GetMapping("/validate")
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
