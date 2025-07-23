package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.DTO.CartItemDTO;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.example.shoppingweb.security.CustomUserDetails;
import org.example.shoppingweb.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private DiscountRepository discountRepository;

    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<String> addToCartAjax(
            @PathVariable Integer productId,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");
        }

        String sizeLabel = body.get("sizeLabel");
        if (sizeLabel == null || sizeLabel.isEmpty()) {
            return ResponseEntity.badRequest().body("Size must be selected.");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Size> sizeOpt = sizeRepository.findBySizeLabel(sizeLabel);

        if (userOpt.isEmpty() || productOpt.isEmpty() || sizeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product or size not found.");
        }

        User user = userOpt.get();
        Product product = productOpt.get();
        Size size = sizeOpt.get();

        Optional<Productsize> productSizeOpt = productSizeRepository.findByProductAndSize(product, size);

        if (productSizeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Size not available for this product.");
        }

        Productsize productSize = productSizeOpt.get();
        if (productSize.getStockQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This size is out of stock.");
        }

        Optional<Cart> existingCart = cartRepository.findByUserAndProductAndSize(user, product, size);

        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + 1);
            cartRepository.save(cart);
        } else {
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setSize(size);
            cart.setQuantity(1);
            cart.setCreatedAt(Instant.now());
            cartRepository.save(cart);
        }

        return ResponseEntity.ok("Added to cart successfully!");
    }


    @PostMapping("/increase/{productId}")
    @ResponseBody
    public ResponseEntity<String> increaseQuantity(
            @PathVariable Integer productId,
            @RequestBody Map<String, String> body,
            @SessionAttribute(name = "currentUser", required = false) User currentUser) {

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        String sizeLabel = body.get("sizeLabel");
        if (sizeLabel == null || sizeLabel.isEmpty()) {
            return ResponseEntity.badRequest().body("Size must be provided");
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Size> sizeOpt = sizeRepository.findBySizeLabel(sizeLabel);

        if (productOpt.isEmpty() || sizeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product or size not found");
        }

        Optional<Cart> cartOpt = cartRepository.findByUserAndProductAndSize(currentUser, productOpt.get(), sizeOpt.get());

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
    public ResponseEntity<String> decreaseQuantity(
            @PathVariable Integer productId,
            @RequestBody Map<String, String> body,
            @SessionAttribute(name = "currentUser", required = false) User currentUser) {

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        String sizeLabel = body.get("sizeLabel");
        if (sizeLabel == null || sizeLabel.isEmpty()) {
            return ResponseEntity.badRequest().body("Size must be provided");
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Size> sizeOpt = sizeRepository.findBySizeLabel(sizeLabel);

        if (productOpt.isEmpty() || sizeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product or size not found");
        }

        Optional<Cart> cartOpt = cartRepository.findByUserAndProductAndSize(currentUser, productOpt.get(), sizeOpt.get());

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
    public ResponseEntity<String> removeItem(
            @PathVariable Integer productId,
            @RequestBody Map<String, String> body,
            @SessionAttribute(name = "currentUser", required = false) User currentUser) {

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        String sizeLabel = body.get("sizeLabel");
        if (sizeLabel == null || sizeLabel.isEmpty()) {
            return ResponseEntity.badRequest().body("Size must be provided");
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Size> sizeOpt = sizeRepository.findBySizeLabel(sizeLabel);

        if (productOpt.isEmpty() || sizeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product or size not found");
        }

        Optional<Cart> cartOpt = cartRepository.findByUserAndProductAndSize(currentUser, productOpt.get(), sizeOpt.get());
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
                    cart.getProduct().getPrice(),
                    cart.getSize().getSizeLabel()
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String shippingAddress,
                           @RequestParam String phone,
                           @RequestParam(required = false) String discountCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login?needLogin=true";
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        Discount discount = null;
        if (discountCode != null && !discountCode.isEmpty()) {
            discount = discountRepository.findByCodeIgnoreCase(discountCode.trim()).orElse(null);
        }

        Order order = orderService.createOrder(user, shippingAddress, phone,discount);
        if (order == null) {
            return "redirect:/cart?emptyCart=true";
        }

        return "redirect:/order/success?id=" + order.getId();
    }
}
