package org.example.shoppingweb.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Cart")
public class Cart {

    @EmbeddedId
    private CartKey id;

    private Integer amount;


}
