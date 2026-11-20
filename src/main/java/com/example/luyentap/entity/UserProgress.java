package com.example.luyentap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "vocabulary_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "is_mastered", nullable = false)
    private boolean isMastered = false; // true = Đã thuộc, false = Chưa thuộc

    // Các trường phục vụ thuật toán Spaced Repetition (SRS - SM-2 / Leitner)
    @Column(name = "repetitions", nullable = false)
    private int repetitions = 0; // Số lần ôn thành công liên tiếp

    @Column(name = "interval_days", nullable = false)
    private int intervalDays = 0; // Khoảng cách ngày đến lần ôn tiếp theo

    @Column(name = "ease_factor", nullable = false)
    private double easeFactor = 2.5; // Hệ số độ dễ mặc định chuẩn SM-2 là 2.5

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt; // Thời điểm ôn gần nhất

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt; // Thời điểm đến hạn cần ôn tiếp theo
}
