package com.example.luyentap.controller;

import com.example.luyentap.dto.VocabularyDTO;
import com.example.luyentap.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
@CrossOrigin
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/topic/{topicId}")
    public List<VocabularyDTO> getByTopicId(@PathVariable Long topicId) {
        return vocabularyService.getByTopicId(topicId);
    }

    @GetMapping("/{id}")
    public VocabularyDTO getById(@PathVariable Long id) {
        return vocabularyService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VocabularyDTO create(@Valid @RequestBody VocabularyDTO dto) {
        return vocabularyService.create(dto);
    }
    @PostMapping("/bulk")
    public ResponseEntity<?> createBulk(@RequestBody List<VocabularyDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Danh sách từ vựng rỗng!"));
        }

        // Lặp qua danh sách và lưu từng từ vựng vào CSDL
        for (VocabularyDTO dto : dtoList) {
            vocabularyService.create(dto);
        }

        return ResponseEntity.ok(Map.of("message", "Đã nhập thành công " + dtoList.size() + " từ vựng!"));
    }
    @PutMapping("/{id}")
    public VocabularyDTO update(@PathVariable Long id, @Valid @RequestBody VocabularyDTO dto) {
        return vocabularyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        vocabularyService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa từ vựng thành công!"));
    }

    @GetMapping("/game")
    public ResponseEntity<List<VocabularyDTO>> getGameVocabularies(
            @RequestParam(required = false) Long topicId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(vocabularyService.getRandomForGame(topicId, limit));
    }

    @GetMapping
    public List<VocabularyDTO> getAll() {
        return vocabularyService.getByTopicId(null);
    }
    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteBulk(@RequestBody List<Long> ids) {
        vocabularyService.deleteBulk(ids);
        return ResponseEntity.ok(Map.of("message", "Đã xóa thành công " + ids.size() + " từ vựng!"));
    }
}