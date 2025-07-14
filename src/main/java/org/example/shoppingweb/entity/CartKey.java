package org.example.shoppingweb.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class CartKey implements Serializable {
    private Integer accountID;
    private Integer productID;

    // Getter - Setter, equals, hashCode
}
