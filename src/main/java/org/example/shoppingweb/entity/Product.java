package org.example.shoppingweb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String image;
    private Double price;
    private String title;
    private String description;

    @Column(name = "cateID")
    private Integer categoryId;

    @Column(name = "sell_ID")
    private Integer sellerId;

    // Getter - Setter
}

