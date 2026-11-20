package com.example.luyentap.dto;

import com.example.luyentap.strategy.ReviewRating;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsReviewRequestDTO {

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotNull(message = "Vocabulary ID không được để trống")
    private Long vocabularyId;

    @NotNull(message = "Topic ID không được để trống")
    private Long topicId;

    @NotNull(message = "Đánh giá chất lượng ghi nhớ (rating) không được để trống")
    private ReviewRating rating; // AGAIN, HARD, GOOD, EASY

    private String strategy; // "SM-2" hoặc "LEITNER" (mặc định SM-2)
}
