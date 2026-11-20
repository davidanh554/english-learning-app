package com.example.luyentap.repository;

import com.example.luyentap.entity.TopicFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicFolderRepository extends JpaRepository<TopicFolder, Long> {
    Optional<TopicFolder> findByName(String name);
}
