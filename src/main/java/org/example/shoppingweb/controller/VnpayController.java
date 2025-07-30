package org.example.shoppingweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.shoppingweb.service.VnpayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VnpayController {
    @Autowired
    private VnpayService vnpayService;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;


    @GetMapping("/payment/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]
                ));

        String secureHash = params.remove("vnp_SecureHash");

        String hashData = params.entrySet().stream()
                .filter(e -> !e.getKey().equals("vnp_SecureHashType"))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String myHash = vnpayService.hmacSHA512(vnpHashSecret, hashData);

        if (myHash.equalsIgnoreCase(secureHash)) {
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");

            if ("00".equals(responseCode)) {
                // thành công
                Long orderId = Long.parseLong(txnRef);
                // Cập nhật đơn hàng nếu cần
                model.addAttribute("message", "Thanh toán thành công");
            } else {
                model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
            }
        } else {
            model.addAttribute("message", "Sai chữ ký bảo mật");
        }

        return "payment-result"; // trả về view hiển thị kết quả
    }


}
