package com.earthworm.repository;

import com.earthworm.model.CoursePack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoursePackRepository extends JpaRepository<CoursePack, String> {
    List<CoursePack> findByShareLevelIgnoreCaseAndArchivedFalseOrderByOrderAsc(String shareLevel);
}
