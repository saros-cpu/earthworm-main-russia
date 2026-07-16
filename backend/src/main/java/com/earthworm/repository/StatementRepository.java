package com.earthworm.repository;

import com.earthworm.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StatementRepository extends JpaRepository<Statement, String> {
    List<Statement> findByCourseIdOrderByOrderAsc(String courseId);
    List<Statement> findByCourseIdAndArchivedFalseOrderByOrderAsc(String courseId);
    long countByCourseIdAndArchivedFalse(String courseId);
    boolean existsByIdAndCourseIdAndArchivedFalse(String id, String courseId);

    @Query("""
            select s.id
            from Statement s
            join s.course course
            join course.coursePack pack
            where s.id in :statementIds
              and s.archived = false
              and course.archived = false
              and pack.archived = false
              and (lower(pack.shareLevel) = 'public'
                   or :administrator = true
                   or pack.creatorId = :userId)
            """)
    List<String> findAccessibleIds(
            @Param("statementIds") List<String> statementIds,
            @Param("userId") String userId,
            @Param("administrator") boolean administrator);
}
