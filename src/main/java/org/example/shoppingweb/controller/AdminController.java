package org.example.shoppingweb.controller;

import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("totalRevenue", 24780); 
        model.addAttribute("totalOrders", 1245);
        model.addAttribute("activeProducts", 342);
        model.addAttribute("customers", 8756);
        return "admin-dashboard"; 
    }


}