package com.example.luyentap.service;

import com.example.luyentap.dto.SrsReviewRequestDTO;
import com.example.luyentap.dto.SrsReviewResponseDTO;
import com.example.luyentap.dto.UserProgressDTO;
import com.example.luyentap.entity.User;
import com.example.luyentap.entity.UserProgress;
import com.example.luyentap.event.VocabularyStudiedEvent;
import com.example.luyentap.repository.UserProgressRepository;
import com.example.luyentap.repository.UserRepository;
import com.example.luyentap.strategy.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final SrsStrategyFactory srsStrategyFactory;
    private final ApplicationEventPublisher eventPublisher;

    public List<UserProgressDTO> getProgressByTopicAndUser(Long topicId, Long userId) {
        return userProgressRepository.findByUserIdAndTopicId(userId, topicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Backward-compatible update progress (cũ)
     */
    public UserProgressDTO updateProgress(UserProgressDTO dto) {
        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(dto.getUserId(), dto.getVocabularyId())
                .orElse(new UserProgress());

        progress.setUserId(dto.getUserId());
        progress.setVocabularyId(dto.getVocabularyId());
        progress.setTopicId(dto.getTopicId());
        progress.setMastered(dto.isMastered());
        progress.setLastReviewedAt(LocalDateTime.now());

        return toDTO(userProgressRepository.save(progress));
    }

    /**
     * Ôn tập thẻ theo thuật toán Spaced Repetition (SM-2 / Leitner)
     * Áp dụng Strategy Pattern & In-process Event-Driven
     */
    @Transactional
    public SrsReviewResponseDTO reviewSrs(SrsReviewRequestDTO request) {
        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(request.getUserId(), request.getVocabularyId())
                .orElseGet(() -> {
                    UserProgress newProgress = new UserProgress();
                    newProgress.setUserId(request.getUserId());
                    newProgress.setVocabularyId(request.getVocabularyId());
                    newProgress.setTopicId(request.getTopicId());
                    newProgress.setEaseFactor(2.5);
                    newProgress.setRepetitions(0);
                    newProgress.setIntervalDays(0);
                    return newProgress;
                });

        // 1. Chuẩn bị Context cho Strategy
        SrsContext context = SrsContext.builder()
                .repetitions(progress.getRepetitions())
                .intervalDays(progress.getIntervalDays())
                .easeFactor(progress.getEaseFactor())
                .rating(request.getRating())
                .baseTime(LocalDateTime.now())
                .build();

        // 2. Lấy Strategy tương ứng (SM-2 hoặc Leitner) qua Factory
        SpacedRepetitionStrategy strategy = srsStrategyFactory.getStrategy(request.getStrategy());
        SrsCalculationResult result = strategy.calculateNextReview(context);

        // 3. Cập nhật tiến độ học mới
        progress.setRepetitions(result.getNewRepetitions());
        progress.setIntervalDays(result.getNewIntervalDays());
        progress.setEaseFactor(result.getNewEaseFactor());
        progress.setNextReviewAt(result.getNextReviewAt());
        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setMastered(result.isMastered());

        userProgressRepository.save(progress);

        // 4. Bắn sự kiện bất đồng bộ (Event-Driven: Gamification & Streak)
        eventPublisher.publishEvent(new VocabularyStudiedEvent(
                this,
                request.getUserId(),
                request.getVocabularyId(),
                request.getTopicId(),
                request.getRating(),
                result.isMastered()
        ));

        // 5. Lấy số liệu Gamification mới nhất của User
        User user = userRepository.findById(request.getUserId()).orElse(null);
        Integer streak = user != null ? user.getStreak() : 0;
        Integer totalXp = user != null ? user.getTotalXp() : 0;

        return SrsReviewResponseDTO.builder()
                .vocabularyId(request.getVocabularyId())
                .topicId(request.getTopicId())
                .mastered(result.isMastered())
                .repetitions(result.getNewRepetitions())
                .intervalDays(result.getNewIntervalDays())
                .easeFactor(result.getNewEaseFactor())
                .nextReviewAt(result.getNextReviewAt())
                .xpEarned(request.getRating().getXpReward())
                .streak(streak)
                .totalXp(totalXp)
                .strategyUsed(result.getStrategyUsed())
                .message(String.format("Lịch ôn tiếp theo sau %d ngày (ngày %s)",
                        result.getNewIntervalDays(),
                        result.getNextReviewAt().toLocalDate()))
                .build();
    }

    /**
     * Lấy danh sách các thẻ từ vựng đến hạn cần ôn hôm nay (SRS Due Cards)
     */
    public List<UserProgressDTO> getDueReviews(Long userId) {
        return userProgressRepository.findDueReviewsForUser(userId, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserProgressDTO> getDueReviewsByTopic(Long userId, Long topicId) {
        return userProgressRepository.findDueReviewsForTopic(userId, topicId, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Thống kê Gamification & Tiến độ học của người dùng
     */
    public Map<String, Object> getUserStats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        long dueCount = userProgressRepository.countDueReviews(userId, LocalDateTime.now());

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("streak", user != null && user.getStreak() != null ? user.getStreak() : 0);
        stats.put("totalXp", user != null && user.getTotalXp() != null ? user.getTotalXp() : 0);
        stats.put("lastStudyDate", user != null ? user.getLastStudyDate() : null);
        stats.put("dueReviewsCount", dueCount);
        return stats;
    }

    private UserProgressDTO toDTO(UserProgress entity) {
        return UserProgressDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .vocabularyId(entity.getVocabularyId())
                .topicId(entity.getTopicId())
                .mastered(entity.isMastered())
                .repetitions(entity.getRepetitions())
                .intervalDays(entity.getIntervalDays())
                .easeFactor(entity.getEaseFactor())
                .lastReviewedAt(entity.getLastReviewedAt())
                .nextReviewAt(entity.getNextReviewAt())
                .build();
    }
}
