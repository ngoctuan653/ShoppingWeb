package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public String showWishlist(Model model, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("wishlistItems", wishlistService.getWishlistByUserId(user.getId()));
        return "wishlist";
    }

    @PostMapping("/remove")
    public String removeFromWishlist(@RequestParam("productId") Integer productId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        wishlistService.removeFromWishlist(user.getId(), productId);
        return "redirect:/wishlist";
    }

    @PostMapping("/move-all-to-cart")
    public String moveAllToCart(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        wishlistService.moveAllToCart(user);
        return "redirect:/cart";
    }

    // ✅ AJAX API: Add to wishlist
    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<String> addToWishlist(@PathVariable("productId") Integer productId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        wishlistService.addToWishlist(user.getId(), productId);
        return ResponseEntity.ok("Added to wishlist");
    }
}
