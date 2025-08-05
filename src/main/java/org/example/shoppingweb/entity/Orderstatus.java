package org.example.shoppingweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "orderstatus")
public class Orderstatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "status_name", nullable = false, length = 50)
    private String statusName;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    public String getCssClass() {
        return switch (statusName) {
            case "Pending" -> "status-pending";
            case "Confirmed" -> "status-processing";
            case "Delivered" -> "status-shipping";
            case "Shipped" -> "status-completed";
            case "Cancelled" -> "status-cancelled";
            default -> "";
        };
    }

}