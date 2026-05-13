package com.earthworm.repository;

import com.earthworm.model.StatementNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StatementNoteRepository extends JpaRepository<StatementNote, String> {
    List<StatementNote> findByUserIdAndStatementId(String userId, String statementId);
    Optional<StatementNote> findByUserIdAndStatementIdAndContent(String userId, String statementId, String content);
}
