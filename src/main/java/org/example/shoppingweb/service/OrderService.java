package org.example.shoppingweb.service;

import jakarta.transaction.Transactional;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    OrderStatusRepository orderStatusRepository;
    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order createOrder(User user, String shippingAddress, String phone) {
        // Lấy giỏ hàng
        List<Cart> cartItems = cartRepository.findByUser(user);
        if (cartItems == null || cartItems.isEmpty()) {
            return null;
        }

        // Tạo order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(Instant.now());
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phone);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        // Lấy trạng thái mặc định
        Orderstatus defaultStatus = orderStatusRepository.findByStatusName("Pending").orElseThrow(() -> new RuntimeException("Order status 'Pending' not found"));
        order.setStatus(defaultStatus);

        // Tính tổng tiền
        BigDecimal total = BigDecimal.ZERO;
        List<Orderdetail> orderDetails = new ArrayList<>();

        for (Cart item : cartItems) {
            Product product = item.getProduct();
            Size size = item.getSize();
            int quantity = item.getQuantity();

            // Tìm Productsize
            Productsize productSize = productSizeRepository.findByProductAndSize(product, size).orElseThrow(() -> new RuntimeException("Size not found for product: " + product.getProductName()));

            // Kiểm tra tồn kho
            if (productSize.getStockQuantity() < quantity) {
                throw new RuntimeException("Not enough stock for product: " + product.getProductName() +
                        " (Size: " + size.getSizeLabel() + ")");
            }

            // Trừ tồn kho
            productSize.setStockQuantity(productSize.getStockQuantity() - quantity);
            productSize.setUpdatedAt(Instant.now());
            productSizeRepository.save(productSize); // cập nhật

            List<Productsize> remainingSizes = productSizeRepository.findByProduct(product);
            int totalStock = remainingSizes.stream().mapToInt(ps -> ps.getStockQuantity() != null ? ps.getStockQuantity() : 0).sum();
            product.setStockQuantity(totalStock);
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);

            // Tạo Orderdetail
            Orderdetail detail = new Orderdetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setSize(size);
            detail.setQuantity(quantity);
            detail.setUnitPrice(product.getPrice());

            orderDetails.add(detail);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalAmount(total);

        // Lưu đơn và chi tiết đơn
        order = orderRepository.save(order);
        orderDetailRepository.saveAll(orderDetails);

        // Xoá giỏ hàng
        cartRepository.deleteAll(cartItems);

        return order;
    }
}

