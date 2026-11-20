package com.example.luyentap.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpacedRepetitionStrategyTest {

    private Sm2SpacedRepetitionStrategy sm2Strategy;
    private LeitnerSpacedRepetitionStrategy leitnerStrategy;
    private SrsStrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        sm2Strategy = new Sm2SpacedRepetitionStrategy();
        leitnerStrategy = new LeitnerSpacedRepetitionStrategy();
        strategyFactory = new SrsStrategyFactory(List.of(sm2Strategy, leitnerStrategy));
    }

    @Test
    @DisplayName("SM-2: Khi đánh giá AGAIN (Quên) -> Repetitions reset về 0, Interval = 1 ngày")
    void testSm2AgainReset() {
        SrsContext context = SrsContext.builder()
                .repetitions(3)
                .intervalDays(15)
                .easeFactor(2.5)
                .rating(ReviewRating.AGAIN)
                .baseTime(LocalDateTime.now())
                .build();

        SrsCalculationResult result = sm2Strategy.calculateNextReview(context);

        assertEquals(0, result.getNewRepetitions());
        assertEquals(1, result.getNewIntervalDays());
        assertFalse(result.isMastered());
        assertTrue(result.getNewEaseFactor() >= 1.3);
    }

    @Test
    @DisplayName("SM-2: Khi đánh giá GOOD (Nhớ tốt) liên tiếp -> Interval tăng dần")
    void testSm2GoodSequence() {
        LocalDateTime now = LocalDateTime.now();

        // Lần 1: Học mới
        SrsContext ctx1 = SrsContext.builder()
                .repetitions(0)
                .intervalDays(0)
                .easeFactor(2.5)
                .rating(ReviewRating.GOOD)
                .baseTime(now)
                .build();
        SrsCalculationResult r1 = sm2Strategy.calculateNextReview(ctx1);
        assertEquals(1, r1.getNewRepetitions());
        assertEquals(1, r1.getNewIntervalDays());

        // Lần 2: Ôn tập lần 2
        SrsContext ctx2 = SrsContext.builder()
                .repetitions(r1.getNewRepetitions())
                .intervalDays(r1.getNewIntervalDays())
                .easeFactor(r1.getNewEaseFactor())
                .rating(ReviewRating.GOOD)
                .baseTime(now)
                .build();
        SrsCalculationResult r2 = sm2Strategy.calculateNextReview(ctx2);
        assertEquals(2, r2.getNewRepetitions());
        assertEquals(6, r2.getNewIntervalDays());

        // Lần 3: Ôn tập lần 3 -> Interval = 6 * 2.5 = 15 ngày
        SrsContext ctx3 = SrsContext.builder()
                .repetitions(r2.getNewRepetitions())
                .intervalDays(r2.getNewIntervalDays())
                .easeFactor(r2.getNewEaseFactor())
                .rating(ReviewRating.GOOD)
                .baseTime(now)
                .build();
        SrsCalculationResult r3 = sm2Strategy.calculateNextReview(ctx3);
        assertEquals(3, r3.getNewRepetitions());
        assertEquals(15, r3.getNewIntervalDays());
        assertTrue(r3.isMastered());
    }

    @Test
    @DisplayName("Leitner: Khi đánh giá GOOD -> Tăng lên 1 hộp")
    void testLeitnerProgression() {
        SrsContext ctx = SrsContext.builder()
                .repetitions(0) // Box 0 (1 ngày)
                .rating(ReviewRating.GOOD)
                .baseTime(LocalDateTime.now())
                .build();

        SrsCalculationResult res = leitnerStrategy.calculateNextReview(ctx);
        assertEquals(1, res.getNewRepetitions()); // Box 1
        assertEquals(3, res.getNewIntervalDays()); // 3 ngày
    }

    @Test
    @DisplayName("SrsStrategyFactory: Lấy đúng Strategy theo tên hoặc fallback về SM-2")
    void testFactoryResolution() {
        SpacedRepetitionStrategy sm2 = strategyFactory.getStrategy("SM-2");
        assertEquals("SM-2", sm2.getStrategyName());

        SpacedRepetitionStrategy leitner = strategyFactory.getStrategy("LEITNER");
        assertEquals("LEITNER", leitner.getStrategyName());

        SpacedRepetitionStrategy fallback = strategyFactory.getStrategy(null);
        assertEquals("SM-2", fallback.getStrategyName());
    }
}
