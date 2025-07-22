package org.example.shoppingweb.DTO;

import lombok.Data;

@Data
public class ContactDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String subject;
    private String message;
}
