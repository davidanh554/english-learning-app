package com.example.luyentap.repository;

import com.example.luyentap.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);
    List<UserProgress> findByUserIdAndTopicId(Long userId, Long topicId);
    void deleteByTopicId(Long topicId);

    // Lấy danh sách từ vựng đã đến hạn hoặc quá hạn ôn tập (SRS Due Cards)
    @Query("SELECT p FROM UserProgress p WHERE p.userId = :userId AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now)")
    List<UserProgress> findDueReviewsForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM UserProgress p WHERE p.userId = :userId AND p.topicId = :topicId AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now)")
    List<UserProgress> findDueReviewsForTopic(@Param("userId") Long userId, @Param("topicId") Long topicId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM UserProgress p WHERE p.userId = :userId AND (p.nextReviewAt IS NULL OR p.nextReviewAt <= :now)")
    long countDueReviews(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
