package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("/order/success")
    public String orderSuccess(@RequestParam("id") Integer orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order-success";
    }

    @GetMapping("/order/history")
    public String viewOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<Order> orderList = orderRepository.findByUser(user);

        Map<Integer, List<Orderdetail>> orderDetailsMap = new HashMap<>();
        for (Order order : orderList) {
            List<Orderdetail> details = orderDetailRepository.findByOrder(order);
            orderDetailsMap.put(order.getId(), details);
        }

        model.addAttribute("orders", orderList);
        model.addAttribute("orderDetailsMap", orderDetailsMap);

        return "order-history";
    }
    
    @GetMapping("/orders/view/{id}")
    public String viewOrderDetail(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();
        Order order = orderRepository.findById(id).orElse(null);

        System.out.println("Current User ID: " + currentUser.getId() + ", Checking order ID: " + id);
        System.out.println("Order found: " + (order != null ? order.getId() : "null"));
        System.out.println("Order User ID: " + (order != null ? order.getUser().getId() : "null"));
        System.out.println("Order User FullName: " + (order != null && order.getUser() != null ? order.getUser().getFullName() : "null"));
        System.out.println("Order Status ID: " + (order != null ? order.getStatus().getId() : "null"));
        System.out.println("Order Status Name: " + (order != null && order.getStatus() != null ? order.getStatus().getStatusName() : "null"));
        System.out.println("Order Details size: " + (order != null ? orderDetailRepository.findByOrder(order).size() : 0));
        if (order != null) {
            for (Orderdetail detail : orderDetailRepository.findByOrder(order)) {
                System.out.println("OrderDetail ID: " + detail.getId() + ", Quantity: " + detail.getQuantity() + ", UnitPrice: " + detail.getUnitPrice());
            }
        }

        if (order == null) {
            model.addAttribute("error", "Order not found with ID: " + id);
            return "order-history";
        } else if (!order.getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("error", "You do not have permission to view this order");
            return "order-history";
        }

        List<Orderdetail> orderDetails = orderDetailRepository.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        System.out.println("Returning template: order-detail");
        return "order-detail";
    }
}
