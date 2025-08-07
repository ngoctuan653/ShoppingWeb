package org.example.shoppingweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private String username;
    private String comment;
    private int rating;
    private Instant createdAt;
    private String adminReply;
    private Instant repliedAt;
}
