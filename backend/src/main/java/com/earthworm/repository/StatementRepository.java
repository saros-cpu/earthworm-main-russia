package com.earthworm.repository;

import com.earthworm.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatementRepository extends JpaRepository<Statement, String> {
    List<Statement> findByCourseIdOrderByOrderAsc(String courseId);
    long countByCourseId(String courseId);
}
