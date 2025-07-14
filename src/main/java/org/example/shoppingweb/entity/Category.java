package org.example.shoppingweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Category")
public class Category {

    @Id
    @Column(name = "cid")
    private int id;

    @Column(name = "cname")
    private String name;

    // Getter - Setter
}
