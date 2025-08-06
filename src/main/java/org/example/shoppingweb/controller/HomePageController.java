package org.example.shoppingweb.controller;


import org.example.shoppingweb.DTO.DTO_Login;
import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute( "loginForm", new DTO_Login());
        return "login";
    }

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new DTO_Signup());
        return "signup";
    }

    @GetMapping("/about")
    public String aboutPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails){
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
        }
        return "about";
    }

    @GetMapping("/contact")
    public String contactPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails){
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
        }
        return "contact";
    }

    @GetMapping("/checkout")
    public String checkoutPage(){
        return "checkout";
    }


    @GetMapping("/admin/chat")
    public String chatPage(Model model) {
        model.addAttribute("activePage", "chat");
        return "admin-chat";
    }

    @GetMapping("/403")
    public String error403() {
        return "403"; // Thymeleaf sẽ tìm 403.html trong templates
    }

}

