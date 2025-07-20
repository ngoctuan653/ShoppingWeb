package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;
import java.util.UUID;

@Controller
public class OAuth2Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/oauth2/success")
    public String oauth2Success(OAuth2AuthenticationToken authentication, HttpSession session) {
        OAuth2User oauthUser = authentication.getPrincipal();
        User user = ((CustomUserDetails) oauthUser).getUser();
        session.setAttribute("userId", user.getId());
        session.setAttribute("currentUser", user);
        return "redirect:/";
    }


    private String generateRandomPassword() {
        return UUID.randomUUID().toString();
    }

    private String generateUniqueUsername() {
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String generateNonDuplicateUsername() {
        String username;
        do {
            username = generateUniqueUsername();
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }

}
