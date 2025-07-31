package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.OrderDTO;
import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.example.shoppingweb.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/order-manage")
    public String orderManagePage(Model model){
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "order-managements";
    }

    @GetMapping("/order/success")
    public String orderSuccess(@RequestParam("id") Integer orderId, Model model) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        model.addAttribute("order", order);
        return "order-success";
    }

    @GetMapping("/order/history")
    public String viewOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<Order> orderList = orderRepository.findByUser(user);

        List<OrderDTO> formattedOrders = orderList.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        Map<Integer, List<Orderdetail>> orderDetailsMap = new HashMap<>();
        for (Order order : orderList) {
            List<Orderdetail> details = orderDetailRepository.findByOrder(order);
            orderDetailsMap.put(order.getId(), details);
        }

        model.addAttribute("orders", formattedOrders);
        model.addAttribute("orderDetailsMap", orderDetailsMap);
        return "order-history";
    }


    @PostMapping("/order/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            orderService.cancelOrder(orderId, userDetails.getUser());
            return ResponseEntity.ok().body(Map.of("message", "Order cancelled successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Order cannot be cancelled!"));
        }
    }


    @PostMapping("/order/{orderId}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmOrder(@PathVariable Integer orderId) {
        try {
            orderService.confirmOrder(orderId);
            return ResponseEntity.ok().body(Map.of("message", "Order confirmed successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/order/{orderId}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Integer orderId, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

}
