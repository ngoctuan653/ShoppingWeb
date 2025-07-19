package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Orderstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<Orderstatus, Integer> {
    Optional<Orderstatus> findById(int id);
}
