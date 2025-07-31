package org.example.shoppingweb.service;

import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public List<User> searchUsersByKeyword(String keyword) {
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }


    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }


    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }


    public void updateUser(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setAddress(user.getAddress());

        if (user.getAvatar() != null && user.getAvatar().length > 0) {
            existing.setAvatar(user.getAvatar());
        }

        userRepository.save(existing);
    }

}
