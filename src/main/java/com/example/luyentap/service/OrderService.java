package com.example.luyentap.service;

import com.example.luyentap.dto.CartDTO;
import com.example.luyentap.dto.CartItemDTO;
import com.example.luyentap.dto.CheckoutRequestDTO;
import com.example.luyentap.entity.Order;
import com.example.luyentap.entity.OrderItem;
import com.example.luyentap.entity.Product;
import com.example.luyentap.entity.User;
import com.example.luyentap.repository.OrderRepository;
import com.example.luyentap.repository.ProductRepository;
import com.example.luyentap.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final GhnService ghnService;

    @Transactional
    public Order checkout(CheckoutRequestDTO dto, HttpSession session, String currentUserEmail) {
        CartDTO cart = cartService.getCart(session);
        if (cart == null || cart.getItemList() == null || cart.getItemList().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống! Vui lòng chọn sản phẩm trước khi thanh toán.");
        }

        // Lọc các sản phẩm được chọn (nếu có danh sách selectedProductIds từ checkbox giỏ hàng)
        List<CartItemDTO> itemsToOrder = new ArrayList<>();
        if (dto.getSelectedProductIds() != null && !dto.getSelectedProductIds().isEmpty()) {
            for (CartItemDTO item : cart.getItemList()) {
                if (dto.getSelectedProductIds().contains(item.getProductId())) {
                    itemsToOrder.add(item);
                }
            }
        } else {
            itemsToOrder.addAll(cart.getItemList());
        }

        if (itemsToOrder.isEmpty()) {
            throw new IllegalStateException("Chưa có sản phẩm nào được chọn để thanh toán!");
        }

        // 1. Tính tiền hàng tạm tính
        double subtotal = 0.0;
        int totalWeight = 0;
        for (CartItemDTO item : itemsToOrder) {
            subtotal += item.getSubtotal();
            totalWeight += 300 * item.getQuantity();
        }

        // 2. Tính tiền cước GHN thực tế
        Double shippingFee = dto.getShippingFee();
        if (shippingFee == null && dto.getToDistrictId() != null && dto.getToWardCode() != null) {
            shippingFee = ghnService.calculateShippingFee(dto.getToDistrictId(), dto.getToWardCode(), totalWeight);
        }
        if (shippingFee == null) {
            shippingFee = 0.0;
        }

        double totalPrice = subtotal + shippingFee;

        // 3. Tìm user nếu đã đăng nhập
        User user = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            user = userRepository.findByEmail(currentUserEmail).orElse(null);
        }

        // 4. Tạo mã vận đơn GHN giả lập (hoặc lấy từ API createOrder GHN)
        String ghnCode = "GHN" + System.currentTimeMillis();

        Order order = Order.builder()
                .user(user)
                .customerName(dto.getCustomerName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .provinceId(dto.getProvinceId())
                .provinceName(dto.getProvinceName())
                .toDistrictId(dto.getToDistrictId())
                .districtName(dto.getDistrictName())
                .toWardCode(dto.getToWardCode())
                .wardName(dto.getWardName())
                .ghnTotalFee(shippingFee)
                .ghnOrderCode(ghnCode)
                .subtotal(subtotal)
                .totalPrice(totalPrice)
                .status("COD".equalsIgnoreCase(dto.getPaymentMethod()) ? "COD_ORDERED" : "PENDING")
                .shippingStatus("READY_TO_PICK")
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "COD")
                .items(new ArrayList<>())
                .build();

        // 5. Thêm từng OrderItem
        for (CartItemDTO cartItem : itemsToOrder) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm ID: " + cartItem.getProductId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(cartItem.getName())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .subtotal(cartItem.getSubtotal())
                    .build();

            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // 6. Xóa các sản phẩm đã đặt khỏi giỏ hàng
        for (CartItemDTO cartItem : itemsToOrder) {
            cartService.removeFromCart(session, cartItem.getProductId());
        }

        log.info(" [LAB 05 ORDER] Đã tạo đơn hàng thành công ID: #{} - Tổng tiền: {} VND (Cước GHN: {})",
                savedOrder.getId(), savedOrder.getTotalPrice(), savedOrder.getGhnTotalFee());

        return savedOrder;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng ID: " + id));
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findByOrderByCreatedAtDesc();
    }
}
