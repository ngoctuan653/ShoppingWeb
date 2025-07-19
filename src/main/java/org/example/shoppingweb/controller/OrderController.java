package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/order/success")
    public String orderSuccess(@RequestParam("id") Integer orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order-success";
    }

    @GetMapping("/order/history")
    public String viewOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<Order> orderList = orderRepository.findByUser(user);
        model.addAttribute("orders", orderList);
        return "order-history";
    }
}
