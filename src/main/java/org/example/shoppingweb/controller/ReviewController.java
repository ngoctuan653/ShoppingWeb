package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.ReviewRequest;
import org.example.shoppingweb.DTO.ReviewResponse;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.Review;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.ReviewRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.example.shoppingweb.service.OrderDetailService;
import org.example.shoppingweb.service.ProductService;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.time.Instant;
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

    @GetMapping("/admin/review-manage")
    public String reviewManagePage(Model model) {
        model.addAttribute("activePage", "reviews");
        return "review-management";
    }


    @GetMapping("/api/order/{orderId}/feedback-items")
    public ResponseEntity<?> getFeedbackItems(@PathVariable Integer orderId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<Orderdetail> details = orderDetailService.findByOrderIdAndUserId(orderId, user.getId());

        List<Map<String, Object>> result = details.stream()
                .filter(d -> !reviewRepository.existsByOrderDetail(d))
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
    public ResponseEntity<?> submitFeedback(@RequestBody List<ReviewRequest> reviews,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();

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
                r.getCreatedAt(),
                r.getAdminReply(),
                r.getRepliedAt()
        ));
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/api/admin/reviews")
//    public ResponseEntity<?> getAllReviewsForAdmin() {
//        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
//
//        List<Map<String, Object>> response = reviews.stream().map(review -> {
//            Map<String, Object> map = new HashMap<>();
//            map.put("id", review.getId());
//            map.put("username", review.getUser().getUsername());
//            map.put("productName", review.getOrderDetail().getProduct().getProductName());
//            map.put("productId", review.getOrderDetail().getProduct().getId());
//            map.put("rating", review.getRating());
//            map.put("comment", review.getComment());
//            map.put("createdAt", review.getCreatedAt());
//            map.put("adminReply", review.getAdminReply());
//            map.put("repliedAt", review.getRepliedAt());
//            return map;
//        }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/api/admin/reviews/{reviewId}/reply")
    public ResponseEntity<?> replyToReview(
            @PathVariable Integer reviewId,
            @RequestBody Map<String, String> body
    ) {
        String replyText = body.get("reply");
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setAdminReply(replyText);
        review.setRepliedAt(Instant.now());
        reviewRepository.save(review);

        return ResponseEntity.ok(Map.of("message", "Phản hồi đã được lưu"));
    }

    @GetMapping("/api/admin/reviews")
    public ResponseEntity<?> getAllReviewsForAdmin(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String status
    ) {
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();

        if (rating != null) {
            reviews = reviews.stream()
                    .filter(r -> r.getRating() == rating)
                    .collect(Collectors.toList());
        }

        if ("replied".equalsIgnoreCase(status)) {
            reviews = reviews.stream()
                    .filter(r -> r.getAdminReply() != null && !r.getAdminReply().isBlank())
                    .collect(Collectors.toList());
        } else if ("pending".equalsIgnoreCase(status)) {
            reviews = reviews.stream()
                    .filter(r -> r.getAdminReply() == null || r.getAdminReply().isBlank())
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> response = reviews.stream().map(review -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", review.getId());
            map.put("username", review.getUser().getUsername());
            map.put("productName", review.getOrderDetail().getProduct().getProductName());
            map.put("productId", review.getOrderDetail().getProduct().getId());
            map.put("rating", review.getRating());
            map.put("comment", review.getComment());
            map.put("createdAt", review.getCreatedAt());
            map.put("adminReply", review.getAdminReply());
            map.put("repliedAt", review.getRepliedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/review-stats")
    public ResponseEntity<Map<String, Object>> getReviewStats() {
        long totalReviews = reviewRepository.count();
        long pendingReviews = reviewRepository.countByAdminReplyIsNullOrAdminReply("");
        double avgRating = reviewRepository.averageRating(); // custom query

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", totalReviews);
        stats.put("pendingReviews", pendingReviews);
        stats.put("averageRating", avgRating);

        return ResponseEntity.ok(stats);
    }


}
