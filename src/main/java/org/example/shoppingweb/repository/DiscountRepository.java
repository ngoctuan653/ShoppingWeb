package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Optional<Discount> findByCodeIgnoreCase(String code);
    Page<Discount> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<Discount> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String code, String desc, Pageable pageable);

    Page<Discount> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatusIgnoreCase(
            String code, String desc, String status, Pageable pageable);

    long countByStatus(String status);

}
