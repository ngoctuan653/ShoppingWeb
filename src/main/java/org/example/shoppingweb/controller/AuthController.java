package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.shoppingweb.DTO.DTO_Login;
import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String processLogin(@ModelAttribute DTO_Login login, HttpSession session, Model model) {
        User user = userRepository.findByEmail(login.getEmail());
        if (user != null && user.getPassword().equals(login.getPassword())) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("currentUser", user);
            return "redirect:/";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("signUpForm") @Valid DTO_Signup userDTO,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please enter valid information");
            return "signup";
        }
        if(!userDTO.getPassword().equals(userDTO.getConfirmPassword())){
            model.addAttribute("error", "Password and confirm password must be the same");
            return "signup";
        }
        try{
            userService.signup(userDTO);
            model.addAttribute("success", "Signup successfully");
            return "redirect:/login";
        }catch (Exception e){
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}
