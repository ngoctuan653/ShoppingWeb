package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.entity.Wishlist;
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

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/wishlist")
@SessionAttributes("currentUser") // Đồng bộ với CartController
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private HttpSession session;

    @GetMapping
    public String showWishlist(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userDetails.getUser();
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
            System.out.println("User from Security: " + (currentUser != null ? currentUser.getUsername() : "null"));
        } else {
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
    @ResponseBody
    public ResponseEntity<String> removeFromWishlistAjax(
            @RequestParam("productId") Integer productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = userDetails != null ? userDetails.getUser() : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        wishlistService.removeFromWishlist(currentUser.getId(), productId);
        return ResponseEntity.ok("Removed from wishlist");
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

    @GetMapping("/sort")
    public String sortWishlist(@RequestParam String sortBy,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "fragments/wishlist-items :: wishlistItemsGrid"; // Trả về rỗng nếu không có user
        }

        List<Wishlist> wishlistItems = wishlistService.getWishlistByUserId(currentUser.getId());

        switch (sortBy) {
            case "priceAsc" -> wishlistItems.sort(Comparator.comparing(w -> w.getProduct().getPrice()));
            case "priceDesc" -> wishlistItems.sort(Comparator.comparing((Wishlist w) -> w.getProduct().getPrice()).reversed());
            case "nameAsc" -> wishlistItems.sort(Comparator.comparing(w -> w.getProduct().getProductName()));
            default -> Collections.reverse(wishlistItems); // recently added
        }

        model.addAttribute("wishlistItems", wishlistItems);
        return "fragments/wishlist-items :: wishlistItemsGrid";
    }
}