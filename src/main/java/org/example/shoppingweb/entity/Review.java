    package org.example.shoppingweb.entity;

    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotNull;
    import lombok.Getter;
    import lombok.Setter;
    import org.hibernate.annotations.ColumnDefault;

    import java.time.Instant;

    @Getter
    @Setter
    @Entity
    @Table(name = "reviews")
    public class Review {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "review_id", nullable = false)
        private Integer id;

        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "product_id", nullable = false)
        private Product product;

        @NotNull
        @Column(name = "rating", nullable = false)
        private Integer rating;

        @Lob
        @Column(name = "comment")
        private String comment;

        @ColumnDefault("CURRENT_TIMESTAMP")
        @Column(name = "created_at")
        private Instant createdAt;

        @ManyToOne
        @JoinColumn(name = "order_detail_id")
        private Orderdetail orderDetail;


        @Lob
        @Column(name = "admin_reply")
        private String adminReply;

        @Column(name = "replied_at")
        private Instant repliedAt;

    }