package org.example.shoppingweb.service;

import jakarta.transaction.Transactional;
import org.example.shoppingweb.DTO.OrderItemRequestDTO;
import org.example.shoppingweb.entity.*;
import org.example.shoppingweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    OrderStatusRepository orderStatusRepository;
    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SizeRepository sizeRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private UserDiscountRepository userDiscountRepository;

    public Order findById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public int countAll() {
        return orderRepository.countBy();
    }

    public int countByStatus(String statusName) {
        return orderRepository.countByStatus_StatusName(statusName);
    }


    public Order findByIdAndUser(Integer orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoặc bạn không có quyền."));
    }

    @Transactional
    public Order createOrderWithItems(User user, String shippingAddress, String phone, String discountCode, List<OrderItemRequestDTO> items, String paymentMethod) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào được chọn.");
        }

        Discount discount = null;
        if (discountCode != null && !discountCode.trim().isEmpty()) {
            discount = discountRepository.findByCodeIgnoreCase(discountCode.trim())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

            Instant now = Instant.now();
            if ((discount.getStartDate() != null && now.isBefore(discount.getStartDate())) ||
                    (discount.getEndDate() != null && now.isAfter(discount.getEndDate()))) {
                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc chưa có hiệu lực.");
            }

            if(discount.getAvailableQuantity() <= 0){
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng.");
            }
            if (userDiscountRepository.existsByUserAndDiscount(user, discount)) {
                throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi.");
            }
        }

        ZonedDateTime nowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phone);
        order.setPaymentMethod(paymentMethod);
        order.setOrderDate(Instant.now());
        order.setCreatedAt(nowInVietnam.toInstant());
        order.setUpdatedAt(Instant.now());

        Orderstatus status = orderStatusRepository.findByStatusName("Pending")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái đơn hàng mặc định"));
        order.setStatus(status);

        BigDecimal totalBeforeDiscount = BigDecimal.ZERO;
        List<Orderdetail> orderDetails = new ArrayList<>();

        // Lưu danh sách cartItems đã xử lý để xóa sau
        List<Cart> cartsToDelete = new ArrayList<>();

        for (OrderItemRequestDTO item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + item.getProductId()));

            Size size = sizeRepository.findBySizeLabelIgnoreCase(item.getSizeLabel())
                    .orElseThrow(() -> new RuntimeException("Size không hợp lệ: " + item.getSizeLabel()));

            Productsize productSize = productSizeRepository.findByProductAndSize(product, size)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy size cho sản phẩm: " + product.getProductName()));

            if (productSize.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Không đủ hàng cho sản phẩm: " + product.getProductName() + " - Size: " + size.getSizeLabel());
            }

            // Trừ tồn kho
            productSize.setStockQuantity(productSize.getStockQuantity() - item.getQuantity());
            productSize.setUpdatedAt(Instant.now());
            productSizeRepository.save(productSize);

            // Cập nhật tổng tồn kho sản phẩm
            List<Productsize> allSizes = productSizeRepository.findByProduct(product);
            int newTotalStock = allSizes.stream().mapToInt(ps -> ps.getStockQuantity() != null ? ps.getStockQuantity() : 0).sum();
            product.setStockQuantity(newTotalStock);
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);

            // Tạo chi tiết đơn hàng
            Orderdetail detail = new Orderdetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setSize(size);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(product.getPrice());

            orderDetails.add(detail);
            totalBeforeDiscount = totalBeforeDiscount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            // Tìm CartItem tương ứng để xóa
            cartRepository.findByUserAndProductAndSize(user, product, size).ifPresent(cartsToDelete::add);
        }

        order.setTotalAmountBeforeDiscount(totalBeforeDiscount);

        BigDecimal total = totalBeforeDiscount;
        if (discount != null && discount.getDiscountPercentage() != null) {
            BigDecimal discountPercent = discount.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal discountAmount = totalBeforeDiscount.multiply(discountPercent);
            total = totalBeforeDiscount.subtract(discountAmount);
            order.setDiscount(discount);
        }

        order.setTotalAmount(total);

        // Lưu đơn hàng và chi tiết
        order = orderRepository.save(order);
        if (discount != null) {
            Userdiscount userDiscount = new Userdiscount();
            userDiscount.setUser(user);
            userDiscount.setDiscount(discount);
            userDiscount.setUsedAt(Instant.now());
            userDiscountRepository.save(userDiscount);

            discount.setAvailableQuantity(discount.getAvailableQuantity() - 1);
            discount.setUpdatedAt(Instant.now());
            discountRepository.save(discount);
        }

        orderDetailRepository.saveAll(orderDetails);

        // Xóa các cart đã mua
        cartRepository.deleteAll(cartsToDelete);

        // Gửi email xác nhận
        emailService.sendOrderConfirmation(user, order, orderDetails, shippingAddress, phone);

        return order;
    }

    public BigDecimal calculateTotalBeforeDiscount(List<OrderItemRequestDTO> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDTO item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + item.getProductId()));
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    public BigDecimal calculateFinalTotal(BigDecimal totalBeforeDiscount, String discountCode) {
        if (discountCode == null || discountCode.trim().isEmpty()) {
            return totalBeforeDiscount;
        }

        Discount discount = discountRepository.findByCodeIgnoreCase(discountCode.trim())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        Instant now = Instant.now();
        if ((discount.getStartDate() != null && now.isBefore(discount.getStartDate())) ||
                (discount.getEndDate() != null && now.isAfter(discount.getEndDate()))) {
            throw new RuntimeException("Mã giảm giá đã hết hạn hoặc chưa có hiệu lực.");
        }

        BigDecimal discountPercent = discount.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return totalBeforeDiscount.subtract(totalBeforeDiscount.multiply(discountPercent));
    }


    @Transactional
    public void cancelOrder(Integer orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra đơn thuộc về user và trạng thái cho phép hủy
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (!order.getStatus().getStatusName().equalsIgnoreCase("Pending")) {
            throw new RuntimeException("Chỉ được hủy đơn hàng khi đang ở trạng thái Pending");
        }

        // Trả hàng về kho
        List<Orderdetail> details = orderDetailRepository.findByOrder(order);
        for (Orderdetail detail : details) {
            Product product = detail.getProduct();
            Size size = detail.getSize();

            Productsize productSize = productSizeRepository.findByProductAndSize(product, size)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong kho"));

            // Cộng lại số lượng
            productSize.setStockQuantity(productSize.getStockQuantity() + detail.getQuantity());
            productSize.setUpdatedAt(Instant.now());
            productSizeRepository.save(productSize);

            // Cập nhật tổng tồn kho của sản phẩm
            List<Productsize> allSizes = productSizeRepository.findByProduct(product);
            int totalStock = allSizes.stream()
                    .mapToInt(ps -> ps.getStockQuantity() != null ? ps.getStockQuantity() : 0)
                    .sum();
            product.setStockQuantity(totalStock);
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);
        }

        // Đổi trạng thái đơn hàng
        Orderstatus cancelledStatus = orderStatusRepository.findByStatusName("Cancelled")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Cancelled"));
        order.setStatus(cancelledStatus);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Integer orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if(!order.getStatus().getStatusName().equals("Pending")){
            throw new RuntimeException("Only pending orders can be confirmed.");
        }

        Orderstatus confirmedStatus = orderStatusRepository.findByStatusName("Confirmed").orElseThrow(() -> new RuntimeException("Order status 'Confirmed' not found"));
        order.setStatus(confirmedStatus);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        emailService.sendOrderConfirmedNotification(order.getUser(), order);

    }

    public void updateOrderStatusById(Integer orderId, Integer statusId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Orderstatus status = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Trạng thái không hợp lệ"));

        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        emailService.sendOrderConfirmedNotification(order.getUser(), order);
    }
}

