package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Discount;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.entity.Userdiscount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDiscountRepository extends JpaRepository<Userdiscount, Integer> {
    boolean existsByUserAndDiscount(User user, Discount discount);
}
