package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.entity.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {
    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userId", currentUser.getId());
            model.addAttribute("roleId", currentUser.getRole().getId());
        }
    }
}
