package com.example.luyentap.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Output Result sau khi Strategy tính toán
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsCalculationResult {
    private int newRepetitions;
    private int newIntervalDays;
    private double newEaseFactor;
    private LocalDateTime nextReviewAt;
    private boolean isMastered;
    private String strategyUsed;
}
