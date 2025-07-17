package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Cart;
import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserAndProduct(User user, Product product);

    List<Cart> findByUser(User user);
    
}
