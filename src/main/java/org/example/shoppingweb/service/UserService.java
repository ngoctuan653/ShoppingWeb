package org.example.shoppingweb.service;

import jakarta.persistence.criteria.Predicate;
import org.example.shoppingweb.DTO.DTO_Signup;
import org.example.shoppingweb.DTO.UserDTO;
import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        if (user.getRole() != null) {
            existing.setRole(user.getRole());
        }

        if (user.getStatus() != null) {
            existing.setStatus(user.getStatus());
        }

        if (user.getAvatar() != null && user.getAvatar().length > 0) {
            existing.setAvatar(user.getAvatar());
        }

        userRepository.save(existing);
    }

    public Page<UserDTO> searchAndFilterUsers(String keyword, Integer roleId, String status, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%");
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%");
                predicates.add(cb.or(namePredicate, emailPredicate));
            }

            if (roleId != null) {
                predicates.add(cb.equal(root.get("role").get("id"), roleId));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> users = userRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return users.map(this::convertToDTO);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus());
        dto.setRoleId(user.getRole().getId());
        dto.setRoleName(user.getRole().getRoleName());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
