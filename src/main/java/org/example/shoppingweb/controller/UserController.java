package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @GetMapping("/profile")
    public String viewUserProfile(HttpSession session, Model model) {
        // Lấy user từ session
        User currentUser = (User) session.getAttribute("currentUser");

        // Nếu không có user trong session => chuyển về login
        if (currentUser == null) {
            return "redirect:/login"; // hoặc trang thông báo lỗi
        }

        // Truyền user vào model để hiển thị trên giao diện
        model.addAttribute("user", currentUser);
        return "profile"; // file HTML hiển thị thông tin user
    }
}
