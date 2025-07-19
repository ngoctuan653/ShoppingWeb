package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
