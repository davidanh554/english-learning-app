package com.example.luyentap.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Input Context cho Strategy Pattern tính toán SRS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsContext {
    private int repetitions;        // Số lần ôn thành công liên tiếp hiện tại
    private int intervalDays;       // Khoảng cách ngày hiện tại
    private double easeFactor;      // Hệ số độ dễ hiện tại (mặc định 2.5)
    private ReviewRating rating;    // Đánh giá lần này của người dùng
    private LocalDateTime baseTime; // Thời điểm làm mốc tính (thường là now)
}
