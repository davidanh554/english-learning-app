package com.example.luyentap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Đại diện cho 1 dòng sản phẩm trong Giỏ hàng (Lab 04)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String name;
    private Double price;
    private int quantity;
    private String category;
    private String imageUrl;
    private boolean isPhysical;

    // Tự động tính thành tiền: price * quantity
    public Double getSubtotal() {
        return (price != null ? price : 0.0) * quantity;
    }
}
