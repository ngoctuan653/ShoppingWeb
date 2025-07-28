package org.example.shoppingweb.service;

import jakarta.transaction.Transactional;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    public List<Wishlist> getWishlistByUserId(Integer userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public boolean existsInWishlist(Integer userId, Integer productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void removeFromWishlist(Integer userId, Integer productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }


    public void addToWishlist(Integer userId, Integer productId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Product> product = productRepository.findById(productId);

        if (user.isPresent() && product.isPresent()) {
            Optional<Wishlist> existing = wishlistRepository.findByUserIdAndProductId(userId, productId);
            if (existing.isEmpty()) {
                Wishlist wishlist = new Wishlist();
                wishlist.setUser(user.get());
                wishlist.setProduct(product.get());
                wishlist.setCreatedAt(Instant.now());
                wishlistRepository.save(wishlist);
            }
        }
    }
}