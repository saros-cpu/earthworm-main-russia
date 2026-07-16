package com.earthworm.repository;

import com.earthworm.model.StudyGroupActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyGroupActivityRepository extends JpaRepository<StudyGroupActivity, String> {
    List<StudyGroupActivity> findByGroupIdOrderByCreatedAtDesc(String groupId);
}