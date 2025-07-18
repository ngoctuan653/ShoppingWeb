package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.DTO.CartItemDTO;
import org.example.shoppingweb.entity.Cart;
import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.CartRepository;
import org.example.shoppingweb.repository.ProductRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
@SessionAttributes("currentUser")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<String> addToCartAjax(@PathVariable Integer productId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isPresent() && productOpt.isPresent()) {
            User user = userOpt.get();
            Product product = productOpt.get();

            Optional<Cart> existingCart = cartRepository.findByUserAndProduct(user, product);
            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();
                cart.setQuantity(cart.getQuantity() + 1);
                cartRepository.save(cart);
            } else {
                Cart cart = new Cart();
                cart.setUser(user);
                cart.setProduct(product);
                cart.setQuantity(1);
                cart.setCreatedAt(Instant.now());
                cartRepository.save(cart);
            }

            return ResponseEntity.ok("Add to cart successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @PostMapping("/increase/{productId}")
    @ResponseBody
    public ResponseEntity<String> increaseQuantity(@PathVariable Integer productId, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        Optional<Cart> cartOpt = cartRepository.findByUserAndProduct(currentUser, productRepository.findById(productId).orElse(null));
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.setQuantity(cart.getQuantity() + 1);
            cartRepository.save(cart);
            return ResponseEntity.ok("Quantity increased");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart item not found");
    }


    @PostMapping("/decrease/{productId}")
    @ResponseBody
    public ResponseEntity<String> decreaseQuantity(@PathVariable Integer productId, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        Optional<Cart> cartOpt = cartRepository.findByUserAndProduct(currentUser, productRepository.findById(productId).orElse(null));
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            if (cart.getQuantity() > 1) {
                cart.setQuantity(cart.getQuantity() - 1);
                cartRepository.save(cart);
            } else {
                cartRepository.delete(cart);
            }
            return ResponseEntity.ok("Quantity decreased or item removed");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart item not found");
    }


    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<String> removeItem(@PathVariable Integer productId, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        Optional<Cart> cartOpt = cartRepository.findByUserAndProduct(currentUser, productRepository.findById(productId).orElse(null));
        cartOpt.ifPresent(cartRepository::delete);
        return ResponseEntity.ok("Item removed");
    }

    @GetMapping("/json")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> getCart(@SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Cart> carts = cartRepository.findByUser(currentUser);
        List<CartItemDTO> result = carts.stream().map(cart -> {
            byte[] imageBytes = cart.getProduct().getImage();
            String base64Image = (imageBytes != null && imageBytes.length > 0)
                    ? "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes) : null;
            return new CartItemDTO(
                    cart.getProduct().getId(),
                    cart.getProduct().getProductName(),
                    base64Image,
                    cart.getQuantity(),
                    cart.getProduct().getPrice()
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
