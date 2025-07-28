package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByUserId(Integer userId);

    void deleteByUserIdAndProductId(Integer userId, Integer productId);

    void deleteAllByUserId(Integer userId);

    Optional<Wishlist> findByUserIdAndProductId(Integer userId, Integer productId);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

}
