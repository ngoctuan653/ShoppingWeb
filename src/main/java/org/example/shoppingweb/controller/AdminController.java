package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.repository.ProductRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
            "shipped"
    );

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model) {
        long totalOrders = orderRepository.count();
        long activeProducts = productRepository.countByStatus("Active");
        long totalCustomers = userRepository.countByRoleId(2);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalCustomers", totalCustomers);

        // Lọc đơn hợp lệ
        List<Order> allOrders = orderRepository.findAll();
        List<Order> validOrders = allOrders.stream()
                .filter(order -> {
                    String status = order.getStatus().getStatusName().toLowerCase();
                    return VALID_ORDER_STATUSES.contains(status);
                })
                .collect(Collectors.toList());

        // Tổng doanh thu từ đơn hợp lệ
        BigDecimal totalRevenue = validOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);

        // Đơn hàng gần đây (hiển thị 5 đơn gần nhất bất kể trạng thái)
        List<Order> recentOrders = orderRepository.findTop5ByOrderByOrderDateDesc();
        model.addAttribute("recentOrders", recentOrders);

        // Doanh thu 6 tháng gần nhất (từ đơn hợp lệ)
        Instant sixMonthsAgo = LocalDateTime.now().minusMonths(5).withDayOfMonth(1)
                .atZone(ZoneId.systemDefault()).toInstant();
        Map<Integer, BigDecimal> revenueByMonth = new TreeMap<>();
        for (Order order : validOrders) {
            if (order.getOrderDate().isBefore(sixMonthsAgo)) continue;

            LocalDateTime ldt = LocalDateTime.ofInstant(order.getOrderDate(), ZoneId.systemDefault());
            int month = ldt.getMonthValue();
            revenueByMonth.put(month,
                    revenueByMonth.getOrDefault(month, BigDecimal.ZERO).add(order.getTotalAmount()));
        }

        List<String> months = revenueByMonth.keySet().stream()
                .map(m -> "Tháng " + m)
                .collect(Collectors.toList());
        List<BigDecimal> revenues = new ArrayList<>(revenueByMonth.values());
        model.addAttribute("months", months);
        model.addAttribute("revenues", revenues);

        // Top sản phẩm bán chạy (từ đơn hợp lệ)
        List<Orderdetail> validDetails = orderDetailRepository.findAll().stream()
                .filter(od -> {
                    Order order = od.getOrder();
                    String status = order.getStatus().getStatusName().toLowerCase();
                    return VALID_ORDER_STATUSES.contains(status);
                })
                .collect(Collectors.toList());

        Map<Product, Long> productSoldMap = validDetails.stream()
                .collect(Collectors.groupingBy(
                        Orderdetail::getProduct,
                        Collectors.summingLong(Orderdetail::getQuantity)
                ));

        // Bảng top 5 sản phẩm
        List<Product> topProducts = productSoldMap.entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Product p = entry.getKey();
                    p.setQuantitySold(entry.getValue().intValue());
                    return p;
                })
                .collect(Collectors.toList());
        model.addAttribute("topProducts", topProducts);

        // Pie chart - top 3 sản phẩm
        List<Map.Entry<Product, Long>> top3 = productSoldMap.entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
        List<String> productNames = top3.stream().map(entry -> entry.getKey().getProductName()).collect(Collectors.toList());
        List<Long> quantities = top3.stream().map(Map.Entry::getValue).collect(Collectors.toList());

        model.addAttribute("productNames", productNames);
        model.addAttribute("quantities", quantities);

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("page", "dashboard");

        return "admin-dashboard";
    }
}
