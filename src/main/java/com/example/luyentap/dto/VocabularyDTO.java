package com.example.luyentap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyDTO {
    private Long id;

    @NotBlank(message = "Từ vựng không được để trống")
    private String word;

    private String loaiTu;
    private String phienAm;

    @NotBlank(message = "Nghĩa không được để trống")
    private String meaning;

    private String wordForms;
    private String paraphrase;
    private String collocationExample;
    private String example;
    private String audioUs;
    private String audioUk;

    @NotNull(message = "Topic ID không được để trống")
    private Long topicId;
}