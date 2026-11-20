package com.example.luyentap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;

    @NotBlank(message = "Câu hỏi không được để trống")
    private String question;

    @NotBlank(message = "Đáp án A không được để trống")
    private String optionA;

    @NotBlank(message = "Đáp án B không được để trống")
    private String optionB;

    private String optionC;
    private String optionD;

    @NotBlank(message = "Đáp án đúng không được để trống")
    private String correctOption;

    @NotNull(message = "Topic ID không được để trống")
    private Long topicId;
}