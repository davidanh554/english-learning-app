package com.example.luyentap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho toàn bộ Giỏ hàng (Lab 04 - Shopping Cart)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Lưu các món hàng theo productId để tìm kiếm O(1)
    @Builder.Default
    private Map<Long, CartItemDTO> items = new LinkedHashMap<>();

    public List<CartItemDTO> getItemList() {
        return new ArrayList<>(items.values());
    }

    // Tổng số lượng món hàng trong giỏ
    public int getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
    }

    // Tổng tiền của cả giỏ hàng (Lab 04 - Giỏ hàng có tính tiền)
    public Double getTotalAmount() {
        return items.values().stream()
                .mapToDouble(CartItemDTO::getSubtotal)
                .sum();
    }
}
