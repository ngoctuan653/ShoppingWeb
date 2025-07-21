package org.example.shoppingweb.DTO;

import lombok.Data;

@Data
public class ProductSizeRequest {
    private String sizeLabel;
    private int stockQuantity;
}
