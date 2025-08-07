package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.Orderstatus;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.repository.OrderStatusRepository;
import org.example.shoppingweb.repository.ReviewRepository;
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
    @Autowired
    private OrderStatusRepository orderStatusRepository;
    @Autowired
    private ReviewRepository reviewRepository;

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
        model.addAttribute("currentUserId", userDetails.getUser().getId());
        model.addAttribute("receiverId", 1);
        boolean allItemsReviewed = orderDetails.stream().allMatch(detail ->
                reviewRepository.existsByOrderDetail(detail)
        );


        model.addAttribute("allItemsReviewed", allItemsReviewed); // truyền vào Thymeleaf
        return "order-detail";
    }

    @GetMapping("/api/order/{orderId}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable Integer orderId,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Order order = orderService.findByIdAndUser(orderId, userDetails.getUser());
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<Orderdetail> orderDetails = orderDetailRepository.findByOrder(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getDisplayCode());
        data.put("customer", order.getUser().getFullName());
        data.put("shippingAddress", order.getShippingAddress());
        data.put("phoneNumber", order.getPhoneNumber());
        data.put("status", order.getStatus().getStatusName());
        data.put("total", order.getTotalAmount());
        if(order.getDiscount() != null){
            data.put("discount", order.getDiscount().getCode() + " (" + order.getDiscount().getDiscountPercentage() + "%)");

        }

        List<Map<String, Object>> items = orderDetails.stream().map(od -> {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", od.getProduct().getProductName());
            item.put("size", od.getSize().getSizeLabel());
            item.put("quantity", od.getQuantity());
            item.put("unitPrice", od.getUnitPrice());
            return item;
        }).toList();

        data.put("items", items);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/admin/order/{orderId}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAdminOrderDetails(@PathVariable Integer orderId) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        List<Orderdetail> orderDetails = orderDetailRepository.findByOrder(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getDisplayCode());
        data.put("customer", order.getUser().getFullName());
        data.put("shippingAddress", order.getShippingAddress());
        data.put("phoneNumber", order.getPhoneNumber());
        data.put("status", order.getStatus().getStatusName());
        data.put("total", order.getTotalAmount());
        data.put("totalBeforeDiscount", order.getTotalAmountBeforeDiscount());

        if (order.getDiscount() != null) {
            data.put("discount", order.getDiscount().getCode() + " (" + order.getDiscount().getDiscountPercentage() + "%)");
        }

        List<Map<String, Object>> items = orderDetails.stream().map(od -> {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", od.getProduct().getProductName());
            item.put("size", od.getSize().getSizeLabel());
            item.put("quantity", od.getQuantity());
            item.put("unitPrice", od.getUnitPrice());
            return item;
        }).toList();

        data.put("items", items);
        return ResponseEntity.ok(data);
    }


    @GetMapping("/order/statuses")
    @ResponseBody
    public List<String> getAllStatuses() {
        return orderStatusRepository.findAll()
                .stream()
                .map(Orderstatus::getStatusName)
                .toList();
    }

}
