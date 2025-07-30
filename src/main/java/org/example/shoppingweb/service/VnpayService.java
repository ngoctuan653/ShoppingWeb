package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class VnpayService {

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.payment-url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    public String createRedirectUrl(Order order) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);

        String amount = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100)) // nhân 100 vì VNPay dùng đơn vị VND * 100
                .toBigInteger()
                .toString();
        vnpParams.put("vnp_Amount", amount);

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(order.getId()));
        vnpParams.put("vnp_OrderInfo", "Payment for order " + order.getId());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        vnpParams.put("vnp_CreateDate", createDate);

        // ✅ In log các tham số gửi đi
        System.out.println("======= VNPay Parameters =======");
        vnpParams.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("======= END Parameters =========");

        // Chuỗi query encode để redirect
        String query = vnpParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        // Chuỗi hash (không encode)
        String hashData = vnpParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue()) // ✔ KHÔNG ENCODE
                .collect(Collectors.joining("&"));

        System.out.println("Hash data string: " + hashData);

        String secureHash = hmacSHA512(vnpHashSecret, hashData);
        System.out.println("Secure Hash: " + secureHash);

        String fullUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
        System.out.println("Final Redirect URL: " + fullUrl);

        return fullUrl;
    }

    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(hashBytes).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HMAC SHA512", e);
        }
    }
}
