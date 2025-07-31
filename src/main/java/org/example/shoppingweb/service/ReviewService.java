package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ReviewRepository reviewRepository;


    public boolean allProductsReviewed(Integer orderId, String username) {
        User user = userService.findByUsername(username);
        List<Orderdetail> details = orderDetailService.findByOrderIdAndUserId(orderId, user.getId());

        return details.stream().allMatch(d ->
                reviewRepository.existsByUserIdAndProductId(user.getId(), d.getProduct().getId()));
    }

}
