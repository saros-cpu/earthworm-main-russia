package com.earthworm.repository;

import com.earthworm.model.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, String> {
    List<StudyGroup> findAllByOrderByCreatedAtDesc();
    List<StudyGroup> findByCreatorId(String creatorId);
}
