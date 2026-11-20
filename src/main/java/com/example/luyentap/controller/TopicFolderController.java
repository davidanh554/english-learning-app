package com.example.luyentap.controller;

import com.example.luyentap.dto.TopicFolderDTO;
import com.example.luyentap.service.TopicFolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@CrossOrigin
public class TopicFolderController {

    private final TopicFolderService topicFolderService;

    @GetMapping
    public List<TopicFolderDTO> getAll() {
        return topicFolderService.getAllFolders();
    }

    @GetMapping("/{id}")
    public TopicFolderDTO getById(@PathVariable Long id) {
        return topicFolderService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopicFolderDTO create(@Valid @RequestBody TopicFolderDTO dto) {
        return topicFolderService.create(dto);
    }

    @PutMapping("/{id}")
    public TopicFolderDTO update(@PathVariable Long id, @Valid @RequestBody TopicFolderDTO dto) {
        return topicFolderService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        topicFolderService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa thư mục chủ đề thành công!"));
    }
}
