package com.example.luyentap.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "vocabulary_id"})
})
@Data
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
    private boolean isMastered; // true = Đã thuộc, false = Chưa thuộc
}