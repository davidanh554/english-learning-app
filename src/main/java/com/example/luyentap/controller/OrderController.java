package com.example.luyentap.controller;

import com.example.luyentap.dto.CheckoutRequestDTO;
import com.example.luyentap.entity.Order;
import com.example.luyentap.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    /**
     * Tạo đơn hàng từ giỏ hàng (Checkout)
     * POST /api/orders/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequestDTO dto,
                                          HttpSession session,
                                          Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Order order = orderService.checkout(dto, session, email);
        return ResponseEntity.ok(order);
    }

    /**
     * Xem chi tiết 1 đơn hàng
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * Xem tất cả đơn hàng (cho Admin)
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
