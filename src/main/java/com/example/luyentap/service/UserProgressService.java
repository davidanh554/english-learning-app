package com.example.luyentap.service;

import com.example.luyentap.dto.UserProgressDTO;
import com.example.luyentap.entity.UserProgress;
import com.example.luyentap.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;

    public List<UserProgressDTO> getProgressByTopicAndUser(Long topicId, Long userId) {
        return userProgressRepository.findByUserIdAndTopicId(userId, topicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserProgressDTO updateProgress(UserProgressDTO dto) {
        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(dto.getUserId(), dto.getVocabularyId())
                .orElse(new UserProgress());

        progress.setUserId(dto.getUserId());
        progress.setVocabularyId(dto.getVocabularyId());
        progress.setTopicId(dto.getTopicId());
        progress.setMastered(dto.isMastered());

        return toDTO(userProgressRepository.save(progress));
    }

    private UserProgressDTO toDTO(UserProgress entity) {
        return new UserProgressDTO(
                entity.getId(),
                entity.getUserId(),
                entity.getVocabularyId(),
                entity.getTopicId(),
                entity.isMastered()
        );
    }
}