package com.example.luyentap.service;

import com.example.luyentap.dto.CartDTO;
import com.example.luyentap.dto.CartItemDTO;
import com.example.luyentap.entity.Product;
import com.example.luyentap.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * Service xử lý nghiệp vụ Giỏ hàng (Lab 04 - Shopping Cart)
 * Lưu trữ trạng thái giỏ hàng qua Session
 */
@Service
@RequiredArgsConstructor
public class CartService {

    public static final String SESSION_CART_KEY = "SHOPPING_CART";
    private final ProductRepository productRepository;

    /**
     * Lấy giỏ hàng từ Session (nếu chưa có thì tạo mới)
     */
    public CartDTO getCart(HttpSession session) {
        CartDTO cart = (CartDTO) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new CartDTO();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        return cart;
    }

    /**
     * Thêm sản phẩm vào giỏ (Lab 04: nếu đã có thì tăng quantity, chưa có thì thêm mới)
     */
    public CartDTO addToCart(HttpSession session, Long productId, int quantity) {
        CartDTO cart = getCart(session);
        int qtyToAdd = quantity <= 0 ? 1 : quantity;

        if (cart.getItems().containsKey(productId)) {
            // Sản phẩm đã có trong giỏ -> Tăng số lượng
            CartItemDTO item = cart.getItems().get(productId);
            item.setQuantity(item.getQuantity() + qtyToAdd);
        } else {
            // Sản phẩm chưa có -> Tìm trong DB và thêm mới vào giỏ
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm có ID: " + productId));

            CartItemDTO newItem = CartItemDTO.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(qtyToAdd)
                    .category(product.getCategory())
                    .imageUrl(product.getImageUrl())
                    .isPhysical(product.isPhysical())
                    .build();

            cart.getItems().put(productId, newItem);
        }

        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ (Lab 04)
     */
    public CartDTO updateQuantity(HttpSession session, Long productId, int newQuantity) {
        CartDTO cart = getCart(session);

        if (cart.getItems().containsKey(productId)) {
            if (newQuantity <= 0) {
                // Nếu số lượng <= 0 thì tự động xóa món hàng
                cart.getItems().remove(productId);
            } else {
                cart.getItems().get(productId).setQuantity(newQuantity);
            }
            session.setAttribute(SESSION_CART_KEY, cart);
        }

        return cart;
    }

    /**
     * Xóa 1 sản phẩm khỏi giỏ hàng (Lab 04)
     */
    public CartDTO removeFromCart(HttpSession session, Long productId) {
        CartDTO cart = getCart(session);
        cart.getItems().remove(productId);
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    /**
     * Xóa rỗng giỏ hàng
     */
    public CartDTO clearCart(HttpSession session) {
        CartDTO cart = new CartDTO();
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }
}
