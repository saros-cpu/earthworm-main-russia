package com.earthworm.repository;

import com.earthworm.model.StatementNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatementNoteRepository extends JpaRepository<StatementNote, String> {
    List<StatementNote> findByUserIdAndStatementId(String userId, String statementId);
}
