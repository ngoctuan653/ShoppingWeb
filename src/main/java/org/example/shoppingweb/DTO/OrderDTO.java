package org.example.shoppingweb.DTO;

import lombok.Data;
import org.example.shoppingweb.entity.Order;

@Data
public class OrderDTO {
    private Order order;
    private String formattedId;

    public OrderDTO(Order order) {
        this.order = order;
        this.formattedId = String.format("OD%08d", order.getId());
    }
}
