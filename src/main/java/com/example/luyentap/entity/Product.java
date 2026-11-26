package com.example.luyentap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "category", length = 100)
    private String category; // "Thẻ Flashcard Giấy", "Gói VIP Số", "Tài Nguyên AI"

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 100;

    @Builder.Default
    @Column(name = "is_physical", nullable = false)
    private boolean isPhysical = true; // true = hàng vật lý có ship, false = gói số kích hoạt online

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
