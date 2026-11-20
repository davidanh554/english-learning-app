package com.example.luyentap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicFolderDTO {
    private Long id;

    @NotBlank(message = "Tên chủ đề không được để trống")
    private String name;

    private String description;
    private String color;
    private LocalDateTime createdAt;
    private Long fileCount; // Số lượng file từ vựng trong folder
}
