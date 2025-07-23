package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Optional<Discount> findByCodeIgnoreCase(String code);
}
