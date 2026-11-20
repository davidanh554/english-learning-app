package com.example.luyentap.strategy;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Triển khai thuật toán Hộp Leitner (Leitner 5-box System)
 * Phân chia từ vựng vào 5 hộp tương ứng với chu kỳ ôn tập cố định:
 * Box 1: 1 ngày | Box 2: 3 ngày | Box 3: 7 ngày | Box 4: 14 ngày | Box 5: 30 ngày
 */
@Component("leitnerStrategy")
public class LeitnerSpacedRepetitionStrategy implements SpacedRepetitionStrategy {

    public static final String STRATEGY_NAME = "LEITNER";
    private static final int[] BOX_INTERVALS = {1, 3, 7, 14, 30};

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public SrsCalculationResult calculateNextReview(SrsContext context) {
        ReviewRating rating = context.getRating();
        int currentBox = Math.max(0, Math.min(context.getRepetitions(), BOX_INTERVALS.length - 1));
        LocalDateTime baseTime = context.getBaseTime() != null ? context.getBaseTime() : LocalDateTime.now();

        int nextBox;
        switch (rating) {
            case AGAIN:
                nextBox = 0; // Trở về Hộp 1
                break;
            case HARD:
                nextBox = currentBox; // Giữ nguyên hộp hiện tại
                break;
            case GOOD:
                nextBox = Math.min(currentBox + 1, BOX_INTERVALS.length - 1); // Lên 1 hộp
                break;
            case EASY:
            default:
                nextBox = Math.min(currentBox + 2, BOX_INTERVALS.length - 1); // Nhảy vọt 2 hộp
                break;
        }

        int intervalDays = BOX_INTERVALS[nextBox];
        LocalDateTime nextReviewAt = baseTime.plusDays(intervalDays);
        boolean isMastered = (nextBox >= 3);

        return SrsCalculationResult.builder()
                .newRepetitions(nextBox)
                .newIntervalDays(intervalDays)
                .newEaseFactor(context.getEaseFactor())
                .nextReviewAt(nextReviewAt)
                .isMastered(isMastered)
                .strategyUsed(STRATEGY_NAME)
                .build();
    }
}
