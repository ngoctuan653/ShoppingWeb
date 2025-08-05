package org.example.shoppingweb.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private Integer id;
    private String productName;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private Integer categoryId;
    private Integer subCategoryId;
    private Integer brandId;
    private String status;
    private List<ProductSizeRequest> sizes;
}