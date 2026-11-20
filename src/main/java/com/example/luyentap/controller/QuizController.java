package com.example.luyentap.controller;

import com.example.luyentap.dto.QuizDTO;
import com.example.luyentap.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@CrossOrigin
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/topic/{topicId}")
    public List<QuizDTO> getByTopicId(@PathVariable Long topicId) {
        return quizService.getByTopicId(topicId);
    }

    @GetMapping("/{id}")
    public QuizDTO getById(@PathVariable Long id) {
        return quizService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizDTO create(@Valid @RequestBody QuizDTO dto) {
        return quizService.create(dto);
    }
}