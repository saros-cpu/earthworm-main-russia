package com.earthworm.repository;

import com.earthworm.model.VocabularyBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VocabularyBookRepository extends JpaRepository<VocabularyBook, String> {
    List<VocabularyBook> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<VocabularyBook> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<VocabularyBook> findByUserIdAndWord(String userId, String word);
    void deleteByUserIdAndWord(String userId, String word);

    @Query("SELECT v FROM VocabularyBook v WHERE v.userId = :userId AND (v.word LIKE %:search% OR v.chinese LIKE %:search%)")
    Page<VocabularyBook> searchByUserId(@Param("userId") String userId, @Param("search") String search, Pageable pageable);
}
