package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser(User user);
}
