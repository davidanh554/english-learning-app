package com.example.luyentap.repository;

import com.example.luyentap.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    List<Vocabulary> findByTopicId(Long topicId);

    long countByTopicId(Long topicId);

    void deleteByTopicId(Long topicId);

    @Query(value = "SELECT * FROM vocabularies WHERE topic_id = :topicId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Vocabulary> findRandomByTopicId(@Param("topicId") Long topicId, @Param("limit") int limit);

    @Query(value = "SELECT * FROM vocabularies ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Vocabulary> findRandom(@Param("limit") int limit);
}