package org.example.shoppingweb.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequestDTO {
    private String shippingAddress;
    private String phone;
    private String discountCode;
    private List<OrderItemRequestDTO> items;
    private String paymentMethod;

}
