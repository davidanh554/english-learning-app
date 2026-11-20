package com.example.luyentap.service;

import com.example.luyentap.dto.TopicDTO;
import com.example.luyentap.entity.Topic;
import com.example.luyentap.entity.TopicFolder;
import com.example.luyentap.repository.QuizRepository;
import com.example.luyentap.repository.TopicFolderRepository;
import com.example.luyentap.repository.TopicRepository;
import com.example.luyentap.repository.UserProgressRepository;
import com.example.luyentap.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicFolderRepository topicFolderRepository;
    private final TopicFolderService topicFolderService;
    private final VocabularyRepository vocabularyRepository;
    private final QuizRepository quizRepository;
    private final UserProgressRepository userProgressRepository;

    public List<TopicDTO> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TopicDTO> getTopicsByFolder(Long folderId) {
        List<Topic> topics;
        if (folderId != null) {
            topics = topicRepository.findByFolderId(folderId);
        } else {
            topics = topicRepository.findAll();
        }
        return topics.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TopicDTO getById(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy file từ vựng id = " + id));
        return toDTO(topic);
    }

    @Transactional
    public TopicDTO create(TopicDTO dto) {
        Topic topic = new Topic();
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setImageUrl(dto.getImageUrl());

        if (dto.getFolderId() != null) {
            topic.setFolderId(dto.getFolderId());
        } else {
            TopicFolder defaultFolder = topicFolderService.getOrCreateDefaultFolder();
            topic.setFolderId(defaultFolder.getId());
        }

        return toDTO(topicRepository.save(topic));
    }

    @Transactional
    public TopicDTO update(Long id, TopicDTO dto) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy file từ vựng id = " + id));
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) {
            topic.setImageUrl(dto.getImageUrl());
        }
        if (dto.getFolderId() != null) {
            topic.setFolderId(dto.getFolderId());
        }
        return toDTO(topicRepository.save(topic));
    }

    @Transactional
    public void delete(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy file từ vựng id = " + id));
        // 1. Xóa tiến độ học tập của người dùng thuộc file này
        userProgressRepository.deleteByTopicId(id);
        // 2. Xóa các câu hỏi trắc nghiệm thuộc file này
        quizRepository.deleteByTopicId(id);
        // 3. Xóa tất cả các từ vựng thuộc file này
        vocabularyRepository.deleteByTopicId(id);
        // 4. Xóa chính file từ vựng
        topicRepository.delete(topic);
    }

    private TopicDTO toDTO(Topic topic) {
        TopicDTO dto = new TopicDTO();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        dto.setImageUrl(topic.getImageUrl());
        dto.setFolderId(topic.getFolderId());

        if (topic.getFolderId() != null) {
            topicFolderRepository.findById(topic.getFolderId())
                    .ifPresent(folder -> dto.setFolderName(folder.getName()));
        }

        long count = vocabularyRepository.countByTopicId(topic.getId());
        dto.setVocabCount(count);

        return dto;
    }
}