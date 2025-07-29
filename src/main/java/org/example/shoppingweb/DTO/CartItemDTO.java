package org.example.shoppingweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartItemDTO {
    private Integer productId;
    private String productName;
    private String imageBase64;
    private int quantity;
    private BigDecimal price;
    private String sizeLabel;
    private boolean outOfStock;
}
