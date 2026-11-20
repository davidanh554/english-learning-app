package com.example.luyentap.strategy;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Triển khai thuật toán SuperMemo SM-2 kinh điển
 * Công thức:
 * EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
 * EF tối thiểu = 1.3
 */
@Component("sm2Strategy")
public class Sm2SpacedRepetitionStrategy implements SpacedRepetitionStrategy {

    public static final String STRATEGY_NAME = "SM-2";
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double DEFAULT_EASE_FACTOR = 2.5;

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public SrsCalculationResult calculateNextReview(SrsContext context) {
        ReviewRating rating = context.getRating();
        int repetitions = context.getRepetitions();
        double currentEf = context.getEaseFactor() <= 0 ? DEFAULT_EASE_FACTOR : context.getEaseFactor();
        int previousInterval = context.getIntervalDays();
        LocalDateTime baseTime = context.getBaseTime() != null ? context.getBaseTime() : LocalDateTime.now();

        int q;
        switch (rating) {
            case AGAIN:
                q = 1; // Quên
                break;
            case HARD:
                q = 3; // Nhớ chật vật
                break;
            case GOOD:
                q = 4; // Nhớ chuẩn
                break;
            case EASY:
            default:
                q = 5; // Dễ
                break;
        }

        // 1. Tính toán Ease Factor mới (EF')
        double newEf = currentEf + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));
        if (newEf < MIN_EASE_FACTOR) {
            newEf = MIN_EASE_FACTOR;
        }
        // Làm tròn 2 chữ số thập phân
        newEf = Math.round(newEf * 100.0) / 100.0;

        int newRepetitions;
        int newIntervalDays;

        // 2. Tính toán Repetitions và Interval (khoảng cách ngày ôn)
        if (q < 3) {
            // Đánh giá Quên (AGAIN): Reset chuỗi về 0, ôn lại vào ngày mai
            newRepetitions = 0;
            newIntervalDays = 1;
        } else {
            // Nhớ thành công (HARD, GOOD, EASY)
            if (repetitions == 0) {
                newIntervalDays = 1;
            } else if (repetitions == 1) {
                newIntervalDays = (rating == ReviewRating.HARD) ? 3 : 6;
            } else {
                double multiplier = (rating == ReviewRating.HARD) ? 1.2 : newEf;
                newIntervalDays = (int) Math.round(Math.max(1, previousInterval) * multiplier);
            }
            newRepetitions = repetitions + 1;
        }

        // Tối đa 365 ngày cho 1 vòng ôn
        if (newIntervalDays > 365) {
            newIntervalDays = 365;
        }

        LocalDateTime nextReviewAt = baseTime.plusDays(newIntervalDays);
        boolean isMastered = (newRepetitions >= 3 && newIntervalDays >= 14);

        return SrsCalculationResult.builder()
                .newRepetitions(newRepetitions)
                .newIntervalDays(newIntervalDays)
                .newEaseFactor(newEf)
                .nextReviewAt(nextReviewAt)
                .isMastered(isMastered)
                .strategyUsed(STRATEGY_NAME)
                .build();
    }
}
