package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailService {
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    public List<Orderdetail> findByOrderIdAndUserId(Integer orderId, Integer userId) {
        return orderDetailRepository.findByOrder_IdAndOrder_User_Id(orderId, userId);
    }
    public Orderdetail findById(Integer id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found"));
    }
}
