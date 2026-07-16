package com.earthworm.repository;

import com.earthworm.model.StudyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, String> {
    List<StudyGroupMember> findByGroupId(String groupId);
    List<StudyGroupMember> findByUserId(String userId);
    Optional<StudyGroupMember> findByGroupIdAndUserId(String groupId, String userId);
    long countByGroupId(String groupId);
    boolean existsByGroupIdAndUserId(String groupId, String userId);
}