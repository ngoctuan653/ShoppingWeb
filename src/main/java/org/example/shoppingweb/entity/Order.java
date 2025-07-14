package org.example.shoppingweb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int id;

    @Column(name = "order_date")
    private java.time.LocalDateTime orderDate;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "total_cost")
    private Double totalCost;

    private Integer status;

    // Getter - Setter
}

