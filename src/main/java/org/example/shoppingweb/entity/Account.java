package org.example.shoppingweb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uID")
    private int id;

    @Column(name = "user")
    private String username;

    @Column(name = "pass")
    private String password;

    private Integer isSell;
    private Integer isAdmin;

    private String email;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    private String address;
    private String fullname;


}