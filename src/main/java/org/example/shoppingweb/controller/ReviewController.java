package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.ReviewRequest;
import org.example.shoppingweb.DTO.ReviewResponse;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.Review;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.ReviewRepository;
import org.example.shoppingweb.service.OrderDetailService;
import org.example.shoppingweb.service.ProductService;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReviewController {
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ReviewRepository reviewRepository;


    @GetMapping("/api/order/{orderId}/feedback-items")
    public ResponseEntity<?> getFeedbackItems(@PathVariable Integer orderId, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Orderdetail> details = orderDetailService.findByOrderIdAndUserId(orderId, user.getId());

        List<Map<String, Object>> result = details.stream()
                .filter(d -> !reviewRepository.existsByOrderDetail(d)) // kiểm tra theo Orderdetail
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", d.getProduct().getId());
                    map.put("productName", d.getProduct().getProductName());
                    map.put("sizeLabel", d.getSize().getSizeLabel());
                    map.put("orderDetailId", d.getId()); // truyền về để lưu lúc submit
                    return map;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/reviews/submit")
    public ResponseEntity<?> submitFeedback(@RequestBody List<ReviewRequest> reviews, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        for (ReviewRequest req : reviews) {
            Review review = new Review();
            review.setUser(user);
            review.setProduct(productService.findById(req.getProductId()));
            review.setRating(req.getRating());
            review.setComment(req.getComment());
            review.setCreatedAt(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
            review.setOrderDetail(orderDetailService.findById(req.getOrderDetailId()));
            reviewRepository.save(review);
        }

        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully!"));
    }


    @GetMapping("/api/reviews")
    public ResponseEntity<?> getPaginatedReviews(
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(name = "rating", required = false) String ratingStr
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage;

        if (ratingStr != null && !ratingStr.isBlank()) {
            int rating = Integer.parseInt(ratingStr);
            reviewPage = reviewRepository.findByProductIdAndRating(productId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findByProductId(productId, pageable);
        }

        // Map to DTO
        Page<ReviewResponse> response = reviewPage.map(r -> new ReviewResponse(
                r.getUser().getUsername(),
                r.getComment(),
                r.getRating(),
                r.getCreatedAt()
        ));

        return ResponseEntity.ok(response);
    }

}
