package org.example.shoppingweb.security;

import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<User> userOptional = userRepository.findWithRoleByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            Role role = roleRepository.findById(2)
                    .orElseThrow(() -> new IllegalStateException("Not found role ID 2"));

            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setUsername(email);
            user.setPassword("");
            user.setRole(role);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setStatus("Active");
            user = userRepository.save(user);
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
