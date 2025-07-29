package org.example.shoppingweb.DTO;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Integer productId;
    private String sizeLabel;
    private int quantity;
}
