package com.example.luyentap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color", length = 50)
    private String color; // Ví dụ: màu sắc thẻ hoặc icon class

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
