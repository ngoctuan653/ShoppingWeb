package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping("/profile")
    public String viewUserProfile(Model model , @AuthenticationPrincipal CustomUserDetails userDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getUser().getId());
            model.addAttribute("receiverId", 1);
        }
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }


    @PostMapping("/update_profile")
    public String updateUserProfile(@RequestParam("full_name") String fullName,
                                    @RequestParam("email") String email,
                                    @RequestParam("phone_number") String phoneNumber,
                                    @RequestParam("address") String address,
                                    @RequestParam("avatar") MultipartFile avatarFile,
                                    HttpSession session,
                                    Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setAddress(address);

        // Xử lý ảnh đại diện dưới dạng byte[]
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                currentUser.setAvatar(avatarFile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Lỗi khi đọc file ảnh.");
                return "profile";
            }
        }

        userRepository.save(currentUser);
        session.setAttribute("currentUser", currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("success", "Update profile successfully!");

        return "profile";
    }

    @GetMapping("/avatar/{id}")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable("id") Integer id) {
        return userRepository.findById(id)
                .map(user -> {
                    byte[] image = user.getAvatar();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_JPEG); // hoặc PNG nếu đúng định dạng
                    return new ResponseEntity<>(image, headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            model.addAttribute("error", "Current password is incorrect.");
            model.addAttribute("user", currentUser);
            return "profile";
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match.");
            model.addAttribute("user", currentUser);
            return "profile";
        }

        // Cập nhật mật khẩu mới sau khi mã hóa
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
        session.setAttribute("currentUser", currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("success", "Password changed successfully!");
        return "profile";
    }



}
