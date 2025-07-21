package org.example.shoppingweb.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProductDTO {
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer subCategoryId;
    private Integer brandId;
    private MultipartFile image;
    private String status;
    private List<String> sizeLabels;
    private List<Integer> sizeQuantities;

}