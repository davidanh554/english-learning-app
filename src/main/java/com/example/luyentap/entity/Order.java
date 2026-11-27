package com.example.luyentap.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    // Địa chỉ hành chính phân cấp theo GHN (Lab 05)
    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name", length = 100)
    private String provinceName;

    @Column(name = "to_district_id")
    private Integer toDistrictId;

    @Column(name = "district_name", length = 100)
    private String districtName;

    @Column(name = "to_ward_code", length = 50)
    private String toWardCode;

    @Column(name = "ward_name", length = 100)
    private String wardName;

    // Cước vận chuyển và mã vận đơn từ GHN
    @Builder.Default
    @Column(name = "ghn_total_fee")
    private Double ghnTotalFee = 0.0;

    @Column(name = "ghn_order_code", length = 50)
    private String ghnOrderCode;

    // Tiền hàng (chưa gồm ship) và Tổng tiền (đã gồm ship)
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    // Trạng thái đơn hàng: PENDING, PAID, COD_ORDERED, CANCELLED
    @Builder.Default
    @Column(name = "status", length = 50)
    private String status = "PENDING";

    // Trạng thái giao hàng GHN: NOT_SHIPPED, READY_TO_PICK, DELIVERING, DELIVERED, CANCELLED
    @Builder.Default
    @Column(name = "shipping_status", length = 50)
    private String shippingStatus = "NOT_SHIPPED";

    // Phương thức thanh toán: COD, MOMO, VNPAY, BANKING
    @Builder.Default
    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "COD";

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (shippingStatus == null) shippingStatus = "NOT_SHIPPED";
        if (ghnTotalFee == null) ghnTotalFee = 0.0;
    }
}
