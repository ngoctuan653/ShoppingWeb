package org.example.shoppingweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
    private Integer roleId;
}
