package org.example.shoppingweb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_details_id")
    private int id;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_price")
    private Double productPrice;

    private Integer amount;

    @Column(name = "order_id")
    private Integer orderId;

    // Getter - Setter
}

