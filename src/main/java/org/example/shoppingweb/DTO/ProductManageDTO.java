package org.example.shoppingweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductManageDTO {
    private Integer id;
    private String productName;
    private BigDecimal price;
    private String status;
    private Integer stockQuantity;
    private Instant createdAt;
    private String categoryName;
    private String subcategoryName;
    private String brandName;
    private List<SizeDTO> sizes;
}
