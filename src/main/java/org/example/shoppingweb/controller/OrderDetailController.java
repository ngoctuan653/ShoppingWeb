package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.example.shoppingweb.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderDetailController {
    @Autowired
    private  OrderService orderService;
    @Autowired
    private  OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public OrderDetailController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/order/detail/{orderId}")
    public String viewOrderDetail(@PathVariable Integer orderId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model){
        Order order = orderService.findByIdAndUser(orderId,userDetails.getUser());
        List<Orderdetail> orderDetails = orderDetailRepository.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        return "order-detail";
    }

    @GetMapping("/api/order/detail/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<Orderdetail> orderDetails = orderDetailRepository.findByOrder(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("customer", order.getUser().getFullName());
        result.put("shippingAddress", order.getShippingAddress());
        result.put("total", order.getTotalAmount());
        result.put("items", orderDetails.stream().map(od -> Map.of(
                "productName", od.getProduct().getProductName(),
                "size", od.getSize().getSizeLabel(),
                "quantity", od.getQuantity(),
                "unitPrice", od.getUnitPrice()
        )).toList());

        return ResponseEntity.ok(result);
    }

}
