package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
<<<<<<< HEAD
import org.example.shoppingweb.DTO.DTO_Login;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
=======
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
>>>>>>> PhamVietHoang
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

<<<<<<< HEAD
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new DTO_Login());
        return "login";
    }
=======
    @Autowired
    private UserService userService;
>>>>>>> PhamVietHoang

    @PostMapping("/login")
    public String processLogin(@ModelAttribute DTO_Login login, HttpSession session, Model model) {
        User user = userRepository.findByEmail(login.getEmail());
        if (user != null && user.getPassword().equals(login.getPassword())) {
<<<<<<< HEAD
=======
            session.setAttribute("userId", user.getId());
>>>>>>> PhamVietHoang
            session.setAttribute("currentUser", user);
            return "redirect:/";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }
<<<<<<< HEAD
    
    @GetMapping("/forgot")
    public String forgotPassword() {
    	return "forgot";
    }
    
    //add code to send password reset link
    
=======

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
>>>>>>> PhamVietHoang
}
