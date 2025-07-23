package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<Orderdetail, Integer> {
    List<Orderdetail> findByOrder(Order order);
}
