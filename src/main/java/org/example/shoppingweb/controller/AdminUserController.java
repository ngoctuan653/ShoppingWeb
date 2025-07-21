package org.example.shoppingweb.controller;

import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public String listUsers(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userService.searchUsersByKeyword(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userService.getAllUsers();
        }
        model.addAttribute("users", users);
        return "user-management";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/update")
    public String updateUser(
            @ModelAttribute User user,
            @RequestParam("avatarFile") MultipartFile avatarFile
    ) throws IOException {
        if (!avatarFile.isEmpty()) {
            user.setAvatar(avatarFile.getBytes());
        }
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

}
