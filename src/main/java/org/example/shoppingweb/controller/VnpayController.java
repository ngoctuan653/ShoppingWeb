package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shoppingweb.DTO.CheckoutRequestDTO;
import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.User;
import org.example.shoppingweb.repository.OrderRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class VnpayController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;


    @GetMapping("/payment/vnpay-return")
    public String handleVNPayReturn(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        String vnp_ResponseCode = params.get("vnp_ResponseCode");

        if ("00".equals(vnp_ResponseCode)) {
            CheckoutRequestDTO request = (CheckoutRequestDTO) session.getAttribute("checkoutRequest");
            Integer userId = (Integer) session.getAttribute("userId");
            User user = userRepository.findById(userId).orElseThrow();

            // Tạo đơn hàng
            Order order = orderService.createOrderWithItems(
                    user,
                    request.getShippingAddress(),
                    request.getPhone(),
                    request.getDiscountCode(),
                    request.getItems(),
                    "VNPAY"
            );
            order.setPaymentStatus("PAID");
            orderRepository.save(order);

            // Xóa session
            session.removeAttribute("checkoutRequest");
            session.removeAttribute("userId");

            return "redirect:/order/success?id=" + order.getId();
        }

        redirectAttributes.addFlashAttribute("message", "Thanh toán thất bại hoặc bị huỷ.");
        return "redirect:/checkout";
    }
}
