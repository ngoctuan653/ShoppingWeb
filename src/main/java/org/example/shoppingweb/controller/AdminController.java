package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.ProductRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Stats Cards
        BigDecimal totalRevenue = orderRepository.findTotalRevenue() != null ? orderRepository.findTotalRevenue() : BigDecimal.ZERO;
        long totalOrders = orderRepository.count();
        long activeProducts = productRepository.countByStatus("Active");
        long totalCustomers = userRepository.countByRoleId(2); 

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalCustomers", totalCustomers);

        // Top Selling Products
        List<Object[]> topProductsData = orderDetailRepository.findTop5ProductsByTotalSold();
        List<Product> topProducts = topProductsData.stream()
                .map(data -> {
                    Product product = (Product) data[0];
                    Long quantitySold = (Long) data[1];
                    product.setQuantitySold(quantitySold.intValue());
                    return product;
                })
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("topProducts", topProducts);

        // Recent Orders
        List<Order> recentOrders = orderRepository.findTop5ByOrderByOrderDateDesc();
        model.addAttribute("recentOrders", recentOrders);

        model.addAttribute("page", "dashboard");

        return "admin-dashboard";
    }
}