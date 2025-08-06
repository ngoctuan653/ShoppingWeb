package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Contact;
import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.example.shoppingweb.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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
        message.setFrom("anhldhe180218@fpt.edu.vn");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
    }

    @Async
    public void sendContactEmail(Contact contact) {
        try {
            String to = "anhldhe180218@fpt.edu.vn";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendOrderConfirmation(User user, Order order, List<Orderdetail> orderDetails, String shippingAddress, String phone) {
        try {
            StringBuilder emailBody = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

            String orderTimeString = formatter.format(order.getCreatedAt());

            emailBody.append("Hello ").append(user.getFullName()).append(",\n\n");
            emailBody.append("Thank you for ordering at StyleLegacy!\n");
            emailBody.append("Order code: ").append(order.getDisplayCode()).append("\n");
            emailBody.append("Time to order: ").append(orderTimeString).append("\n\n");

            emailBody.append("Order information:\n");
            for (Orderdetail detail : orderDetails) {
                emailBody.append("- ").append(detail.getProduct().getProductName())
                        .append(" (Size: ").append(detail.getSize().getSizeLabel())
                        .append(") x ").append(detail.getQuantity())
                        .append(" = ").append(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                        .append(" đ\n");
            }

            if (order.getDiscount() != null) {
                emailBody.append("\nDiscount applied: ").append(order.getDiscount().getCode())
                        .append(" (").append(order.getDiscount().getDiscountPercentage()).append("%)\n");
                emailBody.append("Total before discount: ").append(order.getTotalAmountBeforeDiscount()).append(" đ\n");
            }

            emailBody.append("Total amount to pay: ").append(order.getTotalAmount()).append(" đ\n");
            emailBody.append("Shipping address: ").append(shippingAddress).append("\n");
            emailBody.append("Phone Number: ").append(phone).append("\n\n");

            emailBody.append("Thank you for your purchase!\nStyleLegacy");

            sendOrderConfirmation(user.getEmail(), "Order Confirmation #" + order.getDisplayCode(), emailBody.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendOrderConfirmedNotification(User user, Order order) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

            String orderTimeString = formatter.format(order.getCreatedAt());
            String statusUpdateTimeString = formatter.format(order.getUpdatedAt());
            String status = order.getStatus().getStatusName();

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Hello ").append(user.getFullName()).append(",\n\n");

            // Tùy theo trạng thái
            if ("Confirmed".equalsIgnoreCase(status)) {
                emailBody.append("✅ Your order at StyleLegacy has been **confirmed**.\n\n");
            } else if ("Delivered".equalsIgnoreCase(status)) {
                emailBody.append("🚚 Your order at StyleLegacy has been **Delivered**.\n\n");
            } else {
                emailBody.append("📦 Your order at StyleLegacy has been updated to status: ")
                        .append(status).append("\n\n");
            }

            emailBody.append("🧾 Order Code: ").append(order.getDisplayCode()).append("\n");
            emailBody.append("📅 Created At: ").append(orderTimeString).append("\n");
            emailBody.append("⏱ Status Updated At: ").append(statusUpdateTimeString).append("\n");
            emailBody.append("🔖 Current Status: ").append(status).append("\n\n");

            emailBody.append("We will continue processing your order and keep you updated.\n");
            emailBody.append("Thank you for shopping with StyleLegacy!\n\n");
            emailBody.append("— StyleLegacy Team");

            sendOrderConfirmation(
                    user.getEmail(),
                    "Order Update: " + order.getDisplayCode() + " [" + status + "]",
                    emailBody.toString()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendResetCode(String toEmail, String resetCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reset Password Code");
            message.setText("Your reset code is: " + resetCode);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
