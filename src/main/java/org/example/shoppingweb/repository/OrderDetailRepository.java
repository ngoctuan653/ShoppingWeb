package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Orderdetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<Orderdetail, Integer> {
}
