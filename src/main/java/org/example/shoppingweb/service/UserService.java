package org.example.shoppingweb.service;

import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User signup(DTO_Signup dto) throws Exception {
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            throw new Exception("Email is already exist");
        } else if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new Exception("Username is already exist");
        }
        Role role = roleRepository.findById(2).orElseThrow(() -> new Exception("Role not found"));
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(role);
        Instant now = Instant.now();
        user.setCreatedAt(now);
        return userRepository.save(user);
    }
}
