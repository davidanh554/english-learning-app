package com.example.luyentap.strategy;

/**
 * Mức đánh giá chất lượng ghi nhớ của người học
 */
public enum ReviewRating {
    AGAIN(1, "Quên", 2),    // Không nhớ, cần học lại ngay
    HARD(2, "Khó", 5),      // Nhớ nhưng rất chật vật
    GOOD(3, "Tốt", 10),     // Nhớ chuẩn sau một chút suy nghĩ
    EASY(4, "Dễ", 15);      // Thuộc làu, nhớ ngay lập tức

    private final int score;      // Điểm số quy đổi cho thuật toán (1 - 4)
    private final String label;
    private final int xpReward;   // Điểm kinh nghiệm Gamification

    ReviewRating(int score, String label, int xpReward) {
        this.score = score;
        this.label = label;
        this.xpReward = xpReward;
    }

    public int getScore() {
        return score;
    }

    public String getLabel() {
        return label;
    }

    public int getXpReward() {
        return xpReward;
    }
}
