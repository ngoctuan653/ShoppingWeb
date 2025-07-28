package org.example.shoppingweb.service;

import jakarta.transaction.Transactional;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    @Autowired
    private EmailService emailService;

    public Order findByIdAndUser(Integer orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoặc bạn không có quyền."));
    }

    @Transactional
    public Order createOrder(User user, String shippingAddress, String phone, Discount discount) {
        // Lấy giỏ hàng
        List<Cart> cartItems = cartRepository.findByUser(user);
        if (cartItems == null || cartItems.isEmpty()) {
            return null;
        }
        ZonedDateTime nowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Tạo order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(Instant.now());
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phone);
        order.setCreatedAt(nowInVietnam.toInstant());
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
            productSizeRepository.save(productSize);

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

        order.setTotalAmountBeforeDiscount(total);

        if (discount != null) {
            if (discount.getDiscountPercentage() != null) {
                BigDecimal discountPercent = discount.getDiscountPercentage()
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal discountAmount = total.multiply(discountPercent);
                total = total.subtract(discountAmount);
            }
            order.setDiscount(discount);
        }

        order.setTotalAmount(total);

        order = orderRepository.save(order);
        orderDetailRepository.saveAll(orderDetails);
        cartRepository.deleteAll(cartItems);
        emailService.sendOrderConfirmation(user, order, orderDetails, shippingAddress, phone);
        return order;
    }

    @Transactional
    public void cancelOrder(Integer orderId, User currentUser){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if(!order.getUser().getId().equals(currentUser.getId())){
            throw new RuntimeException("You are not authorized to cancel this order.");
        }
        if(order.getStatus().getId() != 1){
            throw new RuntimeException("Only pending orders can be cancelled.");
        }

        Orderstatus cancelledStatus = orderStatusRepository.findByStatusName("Cancelled")
                .orElseThrow(() -> new RuntimeException("Order status 'Cancelled' not found"));

        order.setStatus(cancelledStatus);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Integer orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if(!order.getStatus().getStatusName().equals("Pending")){
            throw new RuntimeException("Only pending orders can be confirmed.");
        }

        Orderstatus confirmedStatus = orderStatusRepository.findByStatusName("Confirmed").orElseThrow(() -> new RuntimeException("Order status 'Confirmed' not found"));
        order.setStatus(confirmedStatus);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }
}

