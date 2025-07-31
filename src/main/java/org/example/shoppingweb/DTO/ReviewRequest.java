package org.example.shoppingweb.DTO;

import lombok.Data;

@Data
public class ReviewRequest {
    private int productId;
    private String comment;
    private int rating;
}
