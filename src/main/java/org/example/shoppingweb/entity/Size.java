package org.example.shoppingweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "sizes")
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_id", nullable = false)
    private Integer id;

    @jakarta.validation.constraints.Size(max = 20)
    @NotNull
    @Column(name = "size_label", nullable = false, length = 20)
    private String sizeLabel;

    @jakarta.validation.constraints.Size(max = 100)
    @Column(name = "description", length = 100)
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Size(String sizeLabel) {
        this.sizeLabel = sizeLabel;
    }

}