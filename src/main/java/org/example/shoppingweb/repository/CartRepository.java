package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Cart;
import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.entity.Size;
import org.example.shoppingweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserAndProduct(User user, Product product);
    Optional<Cart> findByUserAndProductAndSize(User user, Product product, Size size);

    List<Cart> findByUser(User user);
    void deleteByUser(User user);
    void deleteByUserAndProductInAndSizeIn(User user, List<Product> products, List<Size> sizes);

}
