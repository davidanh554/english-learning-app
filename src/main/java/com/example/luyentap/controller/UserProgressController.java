package com.example.luyentap.controller;

import com.example.luyentap.dto.UserProgressDTO;
import com.example.luyentap.service.UserProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin
public class UserProgressController {

    private final UserProgressService userProgressService;

    @GetMapping("/topic/{topicId}/user/{userId}")
    public List<UserProgressDTO> getProgress(@PathVariable Long topicId, @PathVariable Long userId) {
        return userProgressService.getProgressByTopicAndUser(topicId, userId);
    }

    @PostMapping("/update")
    public UserProgressDTO updateProgress(@Valid @RequestBody UserProgressDTO dto) {
        return userProgressService.updateProgress(dto);
    }
}