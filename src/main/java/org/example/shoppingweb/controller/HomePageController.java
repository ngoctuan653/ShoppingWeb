package org.example.shoppingweb.controller;

<<<<<<< HEAD
import org.springframework.stereotype.Controller;
=======
import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.DTO.DTO_Login;
import org.example.shoppingweb.DTO.DTO_Signup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
>>>>>>> PhamVietHoang
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {
<<<<<<< HEAD
	@GetMapping("/")
	public String home() {
	    return "redirect:/index";
	}
	
//    @GetMapping("/index")
//    public String showIndex() {
//        return "index";
//    }
    
//    @GetMapping("/login")
//    public String showLogin() {
//        return "login";
//    }
    
    @GetMapping("/signup")
    public String showSignUp() {
        return "signup";
    }
}
=======

    @GetMapping("/")
    public String home() {
        return "redirect:/index";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new DTO_Login());
        return "login";
    }

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new DTO_Signup());
        return "signup";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }
}
>>>>>>> PhamVietHoang
