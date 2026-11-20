package com.example.luyentap.repository;

import com.example.luyentap.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);
    List<UserProgress> findByUserIdAndTopicId(Long userId, Long topicId);
    void deleteByTopicId(Long topicId);
}