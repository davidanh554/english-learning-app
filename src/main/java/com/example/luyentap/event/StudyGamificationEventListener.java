package com.example.luyentap.event;

import com.example.luyentap.entity.User;
import com.example.luyentap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Listener theo dõi sự kiện học từ vựng để tính Streak & cộng điểm kinh nghiệm (XP)
 * Áp dụng Observer Pattern kết hợp Event-Driven Architecture
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudyGamificationEventListener {

    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void handleVocabularyStudied(VocabularyStudiedEvent event) {
        if (event.getUserId() == null) return;

        userRepository.findById(event.getUserId()).ifPresent(user -> {
            LocalDate today = LocalDate.now();
            LocalDate lastStudyDate = user.getLastStudyDate();

            // 1. Tính toán Chuỗi ngày học liên tục (Streak)
            int currentStreak = user.getStreak() != null ? user.getStreak() : 0;
            if (lastStudyDate == null) {
                user.setStreak(1);
            } else if (lastStudyDate.equals(today.minusDays(1))) {
                user.setStreak(currentStreak + 1); // Duy trì streak liên tiếp
            } else if (lastStudyDate.isBefore(today.minusDays(1))) {
                user.setStreak(1); // Bị đứt chuỗi, bắt đầu lại
            }
            // Nếu lastStudyDate == today: Giữ nguyên streak vì hôm nay đã tính rồi

            user.setLastStudyDate(today);

            // 2. Cộng điểm kinh nghiệm (XP) theo chất lượng ghi nhớ
            int currentXp = user.getTotalXp() != null ? user.getTotalXp() : 0;
            user.setTotalXp(currentXp + event.getXpEarned());

            userRepository.save(user);

            log.info("[GAMIFICATION EVENT] User ID: {} | +{} XP | Total XP: {} | Streak: {} days",
                    user.getId(), event.getXpEarned(), user.getTotalXp(), user.getStreak());
        });
    }
}
