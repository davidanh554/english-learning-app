package com.example.luyentap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsReviewResponseDTO {
    private Long vocabularyId;
    private Long topicId;
    private boolean mastered;
    private int repetitions;
    private int intervalDays;
    private double easeFactor;
    private LocalDateTime nextReviewAt;
    private int xpEarned;
    private Integer streak;
    private Integer totalXp;
    private String strategyUsed;
    private String message;
}
