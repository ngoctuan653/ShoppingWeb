package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Discount;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.DiscountRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.repository.UserDiscountRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Controller
public class DiscountController {

    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private UserDiscountRepository userDiscountRepository;
    @Autowired
    private OrderRepository orderRepository;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/discount-manage")
    public String discountManagePage(Model model) {
        model.addAttribute("activePage", "discounts");
        return "discount-managements";
    }

    @GetMapping("/api/discounts")
    @ResponseBody
    public ResponseEntity<?> getDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());

        Page<Discount> discountPage;

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasSearch && hasStatus) {
            discountPage = discountRepository
                    .findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatusIgnoreCase(
                            search.trim(), search.trim(), status.trim(), pageable);
        } else if (hasSearch) {
            discountPage = discountRepository
                    .findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            search.trim(), search.trim(), pageable);
        } else if (hasStatus) {
            discountPage = discountRepository.findByStatusIgnoreCase(status.trim(), pageable);
        } else {
            discountPage = discountRepository.findAll(pageable);
        }

        return ResponseEntity.ok(discountPage);
    }

    @GetMapping("/api/discounts/stats")
    public ResponseEntity<?> getDiscountStats() {
        long total = discountRepository.count();
        long active = discountRepository.countByStatus("Active");
        long used = orderRepository.sumUsedDiscountCount(); // Tuỳ logic của bạn

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("used", used);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/discounts/available")
    @ResponseBody
    public ResponseEntity<?> getAvailableDiscounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<Discount> discounts = discountRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        Instant now = Instant.now();

        for (Discount discount : discounts) {
            boolean used = userDiscountRepository.existsByUserAndDiscount(user, discount);
            boolean expired = (discount.getStartDate() != null && now.isBefore(discount.getStartDate()))
                    || (discount.getEndDate() != null && now.isAfter(discount.getEndDate()));
            boolean inactive = !"Active".equalsIgnoreCase(discount.getStatus());
            boolean outOfQuantity = discount.getAvailableQuantity() != null && discount.getAvailableQuantity() <= 0;

            Map<String, Object> map = new HashMap<>();
            map.put("code", discount.getCode());
            map.put("description", discount.getDescription());
            map.put("percentage", discount.getDiscountPercentage());
            map.put("used", used);
            map.put("expired", expired);
            map.put("inactive", inactive);
            map.put("outOfQuantity", outOfQuantity);

            result.add(map);
        }

        return ResponseEntity.ok(result);
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
                    response.put("availableQuantity", discount.getAvailableQuantity());
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
    public ResponseEntity<?> validateDiscount(@RequestParam String code,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        return discountRepository.findByCodeIgnoreCase(code.trim())
                .map(discount -> {
                    Instant now = Instant.now();

                    if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("The discount code has expired or is not yet valid.");
                    }

                    if (!"Active".equalsIgnoreCase(discount.getStatus())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Discount code is currently inactive.");
                    }

                    if (userDetails != null) {
                        User user = userDetails.getUser();

                        if (userDiscountRepository.existsByUserAndDiscount(user, discount)) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("You have already used this code.");
                        }
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("percentage", discount.getDiscountPercentage());
                    response.put("description", discount.getDescription());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid discount code."));
    }

    // Add new discount
    @PostMapping("/api/discounts/add")
    @ResponseBody
    public ResponseEntity<?> addDiscount(@RequestBody Map<String, Object> data) {
        try {
            String code = (String) data.get("code");
            BigDecimal percentage = new BigDecimal(data.get("percentage").toString());
            String description = (String) data.get("description");
            Integer availableQuantity = Integer.parseInt(data.get("availableQuantity").toString());
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

            if (availableQuantity < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Must have at least 1 available quantity"));
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
            discount.setAvailableQuantity(availableQuantity);
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
            Integer availableQuantity = Integer.parseInt(data.get("availableQuantity").toString());
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

            if (availableQuantity < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid available quantity"));
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
            discount.setAvailableQuantity(availableQuantity);
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