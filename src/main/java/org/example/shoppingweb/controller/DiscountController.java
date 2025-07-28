package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Discount;
import org.example.shoppingweb.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class DiscountController {

    @Autowired
    private DiscountRepository discountRepository;

    // Load the discount management page
    @GetMapping("/discount-manage")
    public String discountManagePage(Model model) {
        return "discount-managements";
    }

    // Get all discounts
    @GetMapping("/api/discounts/all")
    @ResponseBody
    public ResponseEntity<?> getAllDiscounts() {
        try {
            List<Discount> discounts = discountRepository.findAll();
            return ResponseEntity.ok(discounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load discounts: " + e.getMessage()));
        }
    }

    // Get discount by ID
    @GetMapping("/api/discounts/{id}")
    @ResponseBody
    public ResponseEntity<?> getDiscountById(@PathVariable Integer id) {
        return discountRepository.findById(id)
                .map(discount -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", discount.getId());
                    response.put("code", discount.getCode());
                    response.put("percentage", discount.getDiscountPercentage());
                    response.put("description", discount.getDescription());
                    response.put("status", discount.getStatus());

                    // Convert Instant to LocalDate for form display
                    LocalDate startDate = discount.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = discount.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate();

                    response.put("startDate", startDate.toString());
                    response.put("endDate", endDate.toString());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Discount not found")));
    }

    // Validate discount code for checkout
    @GetMapping("/api/discounts/validate")
    @ResponseBody
    public ResponseEntity<?> validateDiscount(@RequestParam String code) {
        return discountRepository.findByCodeIgnoreCase(code.trim())
                .map(discount -> {
                    Instant now = Instant.now();
                    if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Discount code has expired or not yet active"));
                    }

                    if (!"Active".equals(discount.getStatus())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Discount code is not active"));
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("percentage", discount.getDiscountPercentage());
                    response.put("description", discount.getDescription());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Invalid discount code")));
    }

    // Add new discount
    @PostMapping("/api/discounts/add")
    @ResponseBody
    public ResponseEntity<?> addDiscount(@RequestBody Map<String, Object> data) {
        try {
            String code = (String) data.get("code");
            BigDecimal percentage = new BigDecimal(data.get("percentage").toString());
            String description = (String) data.get("description");
            LocalDate startDate = LocalDate.parse((String) data.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) data.get("endDate"));

            // Validate input
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Code is required"));
            }

            if (percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Percentage must be between 1 and 100"));
            }

            if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "End date must be after start date"));
            }

            // Check if code already exists
            Optional<Discount> existing = discountRepository.findByCodeIgnoreCase(code.trim());
            if (existing.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Discount code already exists"));
            }

            // Convert to Instant
            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            // Create discount
            Discount discount = new Discount();
            discount.setCode(code.trim().toUpperCase());
            discount.setDiscountPercentage(percentage);
            discount.setDescription(description.trim());
            discount.setStatus("Active");
            discount.setStartDate(startInstant);
            discount.setEndDate(endInstant);

            Discount saved = discountRepository.save(discount);
            return ResponseEntity.ok(Map.of("message", "Discount added successfully", "discount", saved));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add discount: " + e.getMessage()));
        }
    }

    // Update discount
    @PutMapping("/api/discounts/edit")
    @ResponseBody
    public ResponseEntity<?> editDiscount(@RequestBody Map<String, Object> data) {
        try {
            Integer id = Integer.valueOf(data.get("id").toString());
            String code = (String) data.get("code");
            BigDecimal percentage = new BigDecimal(data.get("percentage").toString());
            String description = (String) data.get("description");
            String status = (String) data.get("status");
            LocalDate startDate = LocalDate.parse((String) data.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) data.get("endDate"));

            // Validate input
            if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "End date must be after start date"));
            }

            // Find existing discount
            Optional<Discount> existing = discountRepository.findById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Discount not found"));
            }

            // Check for code conflict
            Optional<Discount> sameCode = discountRepository.findByCodeIgnoreCase(code.trim());
            if (sameCode.isPresent() && !sameCode.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Discount code already exists"));
            }

            // Convert to Instant
            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            // Update discount
            Discount discount = existing.get();
            discount.setCode(code.trim().toUpperCase());
            discount.setDiscountPercentage(percentage);
            discount.setDescription(description.trim());
            discount.setStatus(status);
            discount.setStartDate(startInstant);
            discount.setEndDate(endInstant);

            discountRepository.save(discount);
            return ResponseEntity.ok(Map.of("message", "Discount updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update discount: " + e.getMessage()));
        }
    }

    // Delete discount
    @DeleteMapping("/api/discounts/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDiscount(@PathVariable Integer id) {
        try {
            Optional<Discount> existing = discountRepository.findById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Discount not found"));
            }

            discountRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Discount deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete discount: " + e.getMessage()));
        }
    }
}