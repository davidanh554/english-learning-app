package com.example.luyentap.controller;

import com.example.luyentap.dto.AddToCartRequestDTO;
import com.example.luyentap.dto.CartDTO;
import com.example.luyentap.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller cho Giỏ hàng (Lab 04 - Shopping Cart)
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin
public class CartController {

    private final CartService cartService;

    /**
     * Lấy giỏ hàng và tổng tiền hiện tại: GET /api/cart
     */
    @GetMapping
    public ResponseEntity<CartDTO> getCart(HttpSession session) {
        return ResponseEntity.ok(cartService.getCart(session));
    }

    /**
     * Thêm sản phẩm vào giỏ: POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<CartDTO> addToCart(HttpSession session, @Valid @RequestBody AddToCartRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(session, request.getProductId(), request.getQuantity()));
    }

    /**
     * Cập nhật số lượng của một món: PUT /api/cart/update
     */
    @PutMapping("/update")
    public ResponseEntity<CartDTO> updateQuantity(HttpSession session,
                                                  @RequestParam Long productId,
                                                  @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(session, productId, quantity));
    }

    /**
     * Xóa 1 sản phẩm khỏi giỏ: DELETE /api/cart/remove/{productId}
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartDTO> removeFromCart(HttpSession session, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(session, productId));
    }

    /**
     * Xóa toàn bộ giỏ hàng: DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(HttpSession session) {
        cartService.clearCart(session);
        return ResponseEntity.ok(Map.of("message", "Đã xóa rỗng giỏ hàng thành công!"));
    }
}
