package org.example.shoppingweb.service;

import jakarta.transaction.Transactional;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.CartRepository;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.repository.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
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

    @Transactional
    public Order createOrder(User user, String address, String phoneNumber) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        Optional<Orderstatus> opt = orderStatusRepository.findById(1);
        Orderstatus orderstatus = opt.orElseThrow(() -> new RuntimeException("Order status not found"));
        if (cartItems.isEmpty()) {
            return null;
        }
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(Instant.now());
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setShippingAddress(address);
        order.setPhoneNumber(phoneNumber);
        order.setStatus(orderstatus);
        order.setTotalAmount(cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Order savedOrder = orderRepository.save(order);
        for(Cart cart : cartItems){
            Orderdetail orderDetail = new Orderdetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(cart.getProduct());
            orderDetail.setQuantity(cart.getQuantity());
            orderDetail.setUnitPrice(cart.getProduct().getPrice());
            orderDetailRepository.save(orderDetail);
        }
        cartRepository.deleteByUser(user);
        return savedOrder;
    }
}

