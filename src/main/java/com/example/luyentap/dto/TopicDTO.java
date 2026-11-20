package com.example.luyentap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDTO {
    private Long id;

    @NotBlank(message = "Tên file từ vựng không được để trống")
    private String title;

    private String description;
    private String imageUrl;
    private Long folderId;
    private String folderName;
    private Long vocabCount;

    public TopicDTO(Long id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public TopicDTO(Long id, String title, String description, String imageUrl, Long folderId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.folderId = folderId;
    }
}