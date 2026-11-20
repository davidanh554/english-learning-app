package com.example.luyentap.service;

import com.example.luyentap.dto.VocabularyDTO;
import com.example.luyentap.entity.Vocabulary;
import com.example.luyentap.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    public List<VocabularyDTO> getByTopicId(Long topicId) {
        return vocabularyRepository.findByTopicId(topicId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VocabularyDTO getById(Long id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy từ vựng có ID = " + id));
        return toDTO(vocabulary);
    }

    public VocabularyDTO create(VocabularyDTO dto) {
        Vocabulary vocabulary = toEntity(dto);
        return toDTO(vocabularyRepository.save(vocabulary));
    }

    public VocabularyDTO update(Long id, VocabularyDTO dto) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy từ vựng có ID = " + id));

        vocabulary.setWord(dto.getWord());
        vocabulary.setLoaiTu(dto.getLoaiTu());
        vocabulary.setPhienAm(dto.getPhienAm());
        vocabulary.setMeaning(dto.getMeaning());
        vocabulary.setWordForms(dto.getWordForms());
        vocabulary.setParaphrase(dto.getParaphrase());
        vocabulary.setCollocationExample(dto.getCollocationExample());
        vocabulary.setExample(dto.getExample());
        vocabulary.setAudioUs(dto.getAudioUs());
        vocabulary.setAudioUk(dto.getAudioUk());
        vocabulary.setTopicId(dto.getTopicId());

        return toDTO(vocabularyRepository.save(vocabulary));
    }

    public void delete(Long id) {
        if (!vocabularyRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy từ vựng có ID = " + id);
        }
        vocabularyRepository.deleteById(id);
    }

    // Hàm lấy từ vựng ngẫu nhiên phục vụ trò chơi
    public List<VocabularyDTO> getRandomForGame(Long topicId, int limit) {
        List<Vocabulary> list;
        if (topicId != null && topicId > 0) {
            list = vocabularyRepository.findRandomByTopicId(topicId, limit);
        } else {
            list = vocabularyRepository.findRandom(limit);
        }

        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private VocabularyDTO toDTO(Vocabulary entity) {
        return new VocabularyDTO(
                entity.getId(),
                entity.getWord(),
                entity.getLoaiTu(),
                entity.getPhienAm(),
                entity.getMeaning(),
                entity.getWordForms(),
                entity.getParaphrase(),
                entity.getCollocationExample(),
                entity.getExample(),
                entity.getAudioUs(),
                entity.getAudioUk(),
                entity.getTopicId()
        );
    }

    private Vocabulary toEntity(VocabularyDTO dto) {
        Vocabulary entity = new Vocabulary();
        entity.setId(dto.getId());
        entity.setWord(dto.getWord());
        entity.setLoaiTu(dto.getLoaiTu());
        entity.setPhienAm(dto.getPhienAm());
        entity.setMeaning(dto.getMeaning());
        entity.setWordForms(dto.getWordForms());
        entity.setParaphrase(dto.getParaphrase());
        entity.setCollocationExample(dto.getCollocationExample());
        entity.setExample(dto.getExample());
        entity.setAudioUs(dto.getAudioUs());
        entity.setAudioUk(dto.getAudioUk());
        entity.setTopicId(dto.getTopicId());
        return entity;
    }

    public void deleteBulk(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            vocabularyRepository.deleteAllById(ids);
        }
    }
}