package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser(User user);
    Optional<Order> findByIdAndUser(Integer id, User user);
    List<Order> findTop5ByOrderByOrderDateDesc();

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal findTotalRevenue();

    int countBy();
    int countByStatus_StatusName(String statusName);
    List<Order> findByOrderDateAfter(Instant instant);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.discount IS NOT NULL")
    long sumUsedDiscountCount();
}
