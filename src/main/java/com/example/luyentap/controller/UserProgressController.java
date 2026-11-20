package com.example.luyentap.controller;

import com.example.luyentap.dto.SrsReviewRequestDTO;
import com.example.luyentap.dto.SrsReviewResponseDTO;
import com.example.luyentap.dto.UserProgressDTO;
import com.example.luyentap.service.UserProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /**
     * API Ôn tập ngắt quãng Spaced Repetition (SRS SM-2 / Leitner)
     * POST /api/progress/review-srs
     */
    @PostMapping("/review-srs")
    public ResponseEntity<SrsReviewResponseDTO> reviewSrs(@Valid @RequestBody SrsReviewRequestDTO request) {
        return ResponseEntity.ok(userProgressService.reviewSrs(request));
    }

    /**
     * Lấy các từ vựng đến hạn cần ôn hôm nay của toàn bộ tài khoản
     * GET /api/progress/due/user/{userId}
     */
    @GetMapping("/due/user/{userId}")
    public ResponseEntity<List<UserProgressDTO>> getDueReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(userProgressService.getDueReviews(userId));
    }

    /**
     * Lấy các từ vựng đến hạn cần ôn hôm nay trong 1 chủ đề cụ thể
     * GET /api/progress/due/topic/{topicId}/user/{userId}
     */
    @GetMapping("/due/topic/{topicId}/user/{userId}")
    public ResponseEntity<List<UserProgressDTO>> getDueReviewsByTopic(@PathVariable Long topicId, @PathVariable Long userId) {
        return ResponseEntity.ok(userProgressService.getDueReviewsByTopic(userId, topicId));
    }

    /**
     * Thống kê Streak, Tổng XP, số từ đến hạn ôn
     * GET /api/progress/stats/user/{userId}
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(userProgressService.getUserStats(userId));
    }
}
