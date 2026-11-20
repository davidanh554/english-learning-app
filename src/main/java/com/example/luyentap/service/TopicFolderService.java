package com.example.luyentap.service;

import com.example.luyentap.dto.TopicFolderDTO;
import com.example.luyentap.entity.Topic;
import com.example.luyentap.entity.TopicFolder;
import com.example.luyentap.repository.TopicFolderRepository;
import com.example.luyentap.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicFolderService {

    private final TopicFolderRepository topicFolderRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public List<TopicFolderDTO> getAllFolders() {
        // Tự động chuyển các topic cũ chưa có folderId vào thư mục mặc định
        checkAndMigrateLegacyTopics();

        return topicFolderRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TopicFolderDTO getById(Long id) {
        TopicFolder folder = topicFolderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thư mục chủ đề với id = " + id));
        return toDTO(folder);
    }

    @Transactional
    public TopicFolderDTO create(TopicFolderDTO dto) {
        TopicFolder folder = new TopicFolder();
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription());
        folder.setColor(dto.getColor() != null ? dto.getColor() : "#4f46e5");
        folder.setCreatedAt(LocalDateTime.now());
        return toDTO(topicFolderRepository.save(folder));
    }

    @Transactional
    public TopicFolderDTO update(Long id, TopicFolderDTO dto) {
        TopicFolder folder = topicFolderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thư mục chủ đề với id = " + id));
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription());
        if (dto.getColor() != null) {
            folder.setColor(dto.getColor());
        }
        return toDTO(topicFolderRepository.save(folder));
    }

    @Transactional
    public void delete(Long id) {
        TopicFolder folder = topicFolderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thư mục chủ đề với id = " + id));

        // Di chuyển các file từ vựng bên trong sang thư mục mặc định hoặc xóa
        List<Topic> topicsInFolder = topicRepository.findByFolderId(id);
        if (!topicsInFolder.isEmpty()) {
            TopicFolder defaultFolder = getOrCreateDefaultFolder();
            if (!defaultFolder.getId().equals(id)) {
                for (Topic t : topicsInFolder) {
                    t.setFolderId(defaultFolder.getId());
                }
                topicRepository.saveAll(topicsInFolder);
            }
        }

        topicFolderRepository.delete(folder);
    }

    public TopicFolder getOrCreateDefaultFolder() {
        return topicFolderRepository.findByName("Chủ đề chung")
                .orElseGet(() -> {
                    TopicFolder f = new TopicFolder();
                    f.setName("Chủ đề chung");
                    f.setDescription("Thư mục chứa các file từ vựng mặc định");
                    f.setColor("#6366f1");
                    f.setCreatedAt(LocalDateTime.now());
                    return topicFolderRepository.save(f);
                });
    }

    private void checkAndMigrateLegacyTopics() {
        List<Topic> legacyTopics = topicRepository.findByFolderIdIsNull();
        if (!legacyTopics.isEmpty()) {
            TopicFolder defaultFolder = getOrCreateDefaultFolder();
            for (Topic t : legacyTopics) {
                t.setFolderId(defaultFolder.getId());
            }
            topicRepository.saveAll(legacyTopics);
        }
    }

    private TopicFolderDTO toDTO(TopicFolder folder) {
        long count = topicRepository.countByFolderId(folder.getId());
        return new TopicFolderDTO(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getColor(),
                folder.getCreatedAt(),
                count
        );
    }
}
