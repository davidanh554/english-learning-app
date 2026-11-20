package com.example.luyentap.controller;

import com.example.luyentap.dto.TopicDTO;
import com.example.luyentap.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@CrossOrigin
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public List<TopicDTO> getAll(@RequestParam(required = false) Long folderId) {
        if (folderId != null) {
            return topicService.getTopicsByFolder(folderId);
        }
        return topicService.getAllTopics();
    }

    @GetMapping("/folder/{folderId}")
    public List<TopicDTO> getByFolderId(@PathVariable Long folderId) {
        return topicService.getTopicsByFolder(folderId);
    }

    @GetMapping("/{id}")
    public TopicDTO getById(@PathVariable Long id) {
        return topicService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopicDTO create(@Valid @RequestBody TopicDTO dto) {
        return topicService.create(dto);
    }

    @PutMapping("/{id}")
    public TopicDTO update(@PathVariable Long id, @Valid @RequestBody TopicDTO dto) {
        return topicService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        topicService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa file từ vựng thành công!"));
    }
}