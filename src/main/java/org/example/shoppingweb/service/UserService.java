package org.example.shoppingweb.service;

import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User signup(DTO_Signup dto) throws Exception {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("Email is already exist");
        } else if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("Username is already exist");
        }
        Role role = roleRepository.findById(2).orElseThrow(() -> new Exception("Role not found"));
        User user = new User();
        String password = passwordEncoder.encode(dto.getPassword());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(password);
        user.setRole(role);
        user.setStatus("Active");
        Instant now = Instant.now();
        user.setCreatedAt(now);
        return userRepository.save(user);
    }
}
