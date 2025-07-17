package org.example.shoppingweb.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DTO_Signup {
    @NotBlank
    @Size(min = 1, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 1, max = 100)
    private String fullName;

    @NotBlank
    @Size(min = 1, max = 255)
    private String password;

    @NotBlank
    private String confirmPassword;
}
