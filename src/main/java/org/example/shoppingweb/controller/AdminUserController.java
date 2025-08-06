package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.UserDTO;
import org.example.shoppingweb.entity.Role;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.RoleRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("activePage", "users");
        return "user-management";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateUser(
            @ModelAttribute User user,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile
    ) throws IOException {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            user.setAvatar(avatarFile.getBytes());
        }
        userService.updateUser(user);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/get/{id}")
    @ResponseBody
    public UserDTO getUserDTO(@PathVariable("id") Integer id) {
        User user = userService.getUserById(id);

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus());

        dto.setRoleId(user.getRole() != null ? user.getRole().getId() : 1);

        return dto;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/roles")
    @ResponseBody
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/api")
    @ResponseBody
    public Page<UserDTO> getFilteredUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return userService.searchAndFilterUsers(keyword, roleId, status, page, size);
    }

}
