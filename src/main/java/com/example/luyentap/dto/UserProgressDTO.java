package com.example.luyentap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDTO {
    private Long id;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotNull(message = "Vocabulary ID không được để trống")
    private Long vocabularyId;

    @NotNull(message = "Topic ID không được để trống")
    private Long topicId;

    private boolean mastered;

    // SRS Metadata
    private int repetitions;
    private int intervalDays;
    private double easeFactor;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;
}
