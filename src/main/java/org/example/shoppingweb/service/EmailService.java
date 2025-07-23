package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Contact;
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

    public void sendContactEmail(Contact contact) {
        String to = "viethoang2454@gmail.com";
        String subject = "New Contact: " + contact.getSubject();
        String content = "From: " + contact.getFullName() + "\n"
                + "Email: " + contact.getEmail() + "\n"
                + "Phone: " + (contact.getPhoneNumber() != null ? contact.getPhoneNumber() : "N/A") + "\n"
                + "Message:\n" + contact.getMessage();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        javaMailSender.send(message);
    }

    public void sendOrderConfirmation(User user, Order order, List<Orderdetail> orderDetails, String shippingAddress, String phone) {
        StringBuilder emailBody = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        String orderTimeString = formatter.format(order.getCreatedAt());  // dạng "21/07/2025 14:30:00"


        emailBody.append("Hello ").append(user.getFullName()).append(",\n\n");
        emailBody.append("Thank you for ordering at StyleLegacy!\n");
        emailBody.append("Order code: ").append(order.getId()).append("\n");
        emailBody.append("Time to order: ").append(orderTimeString).append("\n\n");

        emailBody.append("Order information:\n");
        for (Orderdetail detail : orderDetails) {
            emailBody.append("- ").append(detail.getProduct().getProductName())
                    .append(" (Size: ").append(detail.getSize().getSizeLabel())
                    .append(") x ").append(detail.getQuantity())
                    .append(" = ").append(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                    .append(" $\n");
        }

        emailBody.append("\nTotal amount: ").append(order.getTotalAmount()).append(" đ\n");
        emailBody.append("Shipping address: ").append(shippingAddress).append("\n");
        emailBody.append("Phone Number: ").append(phone).append("\n\n");
        emailBody.append("Thank you for your purchase.!\nStyleLegacy");

        sendOrderConfirmation(user.getEmail(), "Order Confirmation #" + order.getId(), emailBody.toString());
    }

    public void sendResetCode(String toEmail, String resetCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset Password Code");
        message.setText("Your reset code is: " + resetCode);
        javaMailSender.send(message);
    }
}
