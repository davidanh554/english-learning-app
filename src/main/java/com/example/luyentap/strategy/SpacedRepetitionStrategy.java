package com.example.luyentap.strategy;

/**
 * Strategy Interface định nghĩa hợp đồng tính toán lịch ôn tập ngắt quãng (SRS)
 */
public interface SpacedRepetitionStrategy {

    /**
     * Tên định danh thuật toán (VD: "SM-2", "LEITNER")
     */
    String getStrategyName();

    /**
     * Thực hiện tính toán khoảng cách ôn tập, hệ số nhớ và thời điểm ôn tiếp theo
     *
     * @param context dữ liệu trạng thái hiện tại của thẻ học và đánh giá của user
     * @return kết quả tính toán mới
     */
    SrsCalculationResult calculateNextReview(SrsContext context);
}
