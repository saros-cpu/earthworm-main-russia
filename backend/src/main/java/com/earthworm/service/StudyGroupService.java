package com.earthworm.service;

import com.earthworm.model.StudyGroup;
import com.earthworm.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudyGroupService {
    private final StudyGroupRepository repository;

    public StudyGroupService(StudyGroupRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> listGroups() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> createGroup(String userId, String name, String description) {
        StudyGroup g = new StudyGroup();
        g.setId(UUID.randomUUID().toString());
        g.setName(name);
        g.setDescription(description);
        g.setCreatorId(userId);
        g.setMemberCount(1);
        g.setInviteCode(UUID.randomUUID().toString().substring(0, 8));
        repository.save(g);
        return toMap(g);
    }

    public Map<String, Object> getGroup(String id) {
        return repository.findById(id).map(this::toMap).orElse(Map.of());
    }

    public List<Map<String, Object>> getUserGroups(String userId) {
        return repository.findByCreatorId(userId).stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(StudyGroup g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("name", g.getName());
        m.put("description", g.getDescription());
        m.put("cover", g.getCover());
        m.put("creatorId", g.getCreatorId());
        m.put("memberCount", g.getMemberCount());
        m.put("inviteCode", g.getInviteCode());
        m.put("createdAt", g.getCreatedAt() == null ? null : g.getCreatedAt().toString());
        return m;
    }
}
