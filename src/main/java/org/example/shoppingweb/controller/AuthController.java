package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.service.EmailService;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Random;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession session;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/verify-reset-code")
    public String showVerifyPage() {
        return "reset-code";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage() {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("signUpForm") @Valid DTO_Signup userDTO,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please enter valid information");
            return "signup";
        }
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            model.addAttribute("error", "Password and confirm password must be the same");
            return "signup";
        }
        try {
            userService.signup(userDTO);
            model.addAttribute("message", "Signup successfully");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your email is not registered in the system!");
            return "redirect:/forgot-password";
        }
        String code = String.format("%06d", new Random().nextInt(999999));
        session.setAttribute("resetEmail", email);
        session.setAttribute("resetCode", code);
        session.setAttribute("resetCodeTime", System.currentTimeMillis());
        emailService.sendResetCode(email, code);
        redirectAttributes.addFlashAttribute("message", "Reset code has been sent to your email.");
        return "redirect:/verify-reset-code";
    }

    @PostMapping("/verify-reset-code")
    public String verifyResetCode(HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();

        String codeFromSession = (String) session.getAttribute("resetCode");
        Long codeSentTime = (Long) session.getAttribute("resetCodeTime");

        String code = request.getParameter("code1") +
                request.getParameter("code2") +
                request.getParameter("code3") +
                request.getParameter("code4") +
                request.getParameter("code5") +
                request.getParameter("code6");

        long now = System.currentTimeMillis();
        if (codeFromSession == null || codeSentTime == null) {
            redirectAttributes.addFlashAttribute("error", "Please request a new code.");
            return "redirect:/verify-reset-code";
        }

        if (now - codeSentTime > 5 * 60 * 1000) {
            redirectAttributes.addFlashAttribute("error", "Reset code has expired. Please resend.");
            return "redirect:/verify-reset-code";
        }

        if (!code.equals(codeFromSession)) {
            redirectAttributes.addFlashAttribute("error", "Incorrect verification code.");
            return "redirect:/verify-reset-code";
        }

        return "redirect:/reset-password";
    }

    @PostMapping("/resend-code")
    public String resendResetCode(RedirectAttributes redirectAttributes, HttpSession session) {
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please enter your email again.");
            return "redirect:/forgot-password";
        }

        session.removeAttribute("resetCode");
        session.removeAttribute("resetCodeTime");

        String newCode = String.format("%06d", new Random().nextInt(999999));
        session.setAttribute("resetCode", newCode);
        session.setAttribute("resetCodeTime", System.currentTimeMillis());

        emailService.sendResetCode(email, newCode);
        redirectAttributes.addFlashAttribute("message", "A new code has been sent to your email.");

        return "redirect:/verify-reset-code";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please request reset again.");
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/reset-password";
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/forgot-password";
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        session.removeAttribute("resetEmail");
        session.removeAttribute("resetCode");
        session.removeAttribute("resetCodeTime");

        redirectAttributes.addFlashAttribute("message", "Password reset successfully!");
        return "redirect:/login";
    }
}
