package org.example.shoppingweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTO_Product {
    private Integer id;
    private String name;
    private String description;
    private String base64Image;
    private BigDecimal price;

}
