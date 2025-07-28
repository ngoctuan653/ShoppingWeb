package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.security.CustomUserDetails;
import org.example.shoppingweb.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/wishlist")
@SessionAttributes("currentUser") // Đồng bộ với CartController
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private HttpSession session; // Thêm để fallback nếu cần

    @GetMapping
    public String showWishlist(Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userDetails.getUser();
            System.out.println("User from Security: " + (currentUser != null ? currentUser.getUsername() : "null"));
        } else {
            // Fallback to session if Security context fails
            currentUser = (User) session.getAttribute("currentUser");
            System.out.println("User from Session: " + (currentUser != null ? currentUser.getUsername() : "null"));
        }
        if (currentUser == null) {
            System.out.println("No user found, redirecting to /login");
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("wishlistItems", wishlistService.getWishlistByUserId(currentUser.getId()));
        System.out.println("Model currentUser set: " + currentUser.getUsername());
        return "wishlist";
    }

    @PostMapping("/remove")
    public String removeFromWishlist(@RequestParam("productId") Integer productId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        wishlistService.removeFromWishlist(currentUser.getId(), productId);
        return "redirect:/wishlist";
    }

    @PostMapping("/move-all-to-cart")
    public String moveAllToCart(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        wishlistService.moveAllToCart(currentUser);
        return "redirect:/cart";
    }

    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<String> addToWishlist(@PathVariable("productId") Integer productId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        wishlistService.addToWishlist(currentUser.getId(), productId);
        return ResponseEntity.ok("Added to wishlist");
    }

    @PostMapping("/move-to-cart/{productId}")
    public String moveToCart(@PathVariable("productId") Integer productId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";
        wishlistService.moveToCart(currentUser.getId(), productId);
        return "redirect:/wishlist";
    }
}