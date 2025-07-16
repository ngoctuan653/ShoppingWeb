package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.DTO.DTO_Login;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new DTO_Login());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute DTO_Login login, HttpSession session, Model model) {
        User user = userRepository.findByEmail(login.getEmail());
        if (user != null && user.getPassword().equals(login.getPassword())) {
            session.setAttribute("currentUser", user);
            return "redirect:/";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }
}
