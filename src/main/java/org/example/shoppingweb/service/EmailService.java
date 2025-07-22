package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOrderConfirmation(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("viethoang2454@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
    }

    public void sendOrderConfirmation(User user, Order order, List<Orderdetail> orderDetails, String shippingAddress, String phone) {
        StringBuilder emailBody = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        String orderTimeString = formatter.format(order.getCreatedAt());  // dạng "21/07/2025 14:30:00"


        emailBody.append("Xin chào ").append(user.getFullName()).append(",\n\n");
        emailBody.append("Cảm ơn bạn đã đặt hàng tại StyleLegacy!\n");
        emailBody.append("Mã đơn hàng: ").append(order.getId()).append("\n");
        emailBody.append("Thời gian đặt: ").append(orderTimeString).append("\n\n");

        emailBody.append("Thông tin đơn hàng:\n");
        for (Orderdetail detail : orderDetails) {
            emailBody.append("- ").append(detail.getProduct().getProductName())
                    .append(" (Size: ").append(detail.getSize().getSizeLabel())
                    .append(") x ").append(detail.getQuantity())
                    .append(" = ").append(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                    .append(" đ\n");
        }

        emailBody.append("\nTổng tiền: ").append(order.getTotalAmount()).append(" đ\n");
        emailBody.append("Địa chỉ giao hàng: ").append(shippingAddress).append("\n");
        emailBody.append("SĐT: ").append(phone).append("\n\n");
        emailBody.append("Cảm ơn bạn đã mua hàng!\nStyleLegacy");

        sendOrderConfirmation(user.getEmail(), "Xác nhận đơn hàng #" + order.getId(), emailBody.toString());
    }
}
