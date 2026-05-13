package com.earthworm.repository;

import com.earthworm.model.VocabularyBook;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VocabularyBookRepository extends JpaRepository<VocabularyBook, String> {
    List<VocabularyBook> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<VocabularyBook> findByUserIdAndWord(String userId, String word);
    void deleteByUserIdAndWord(String userId, String word);
}
