package com.earthworm.service;

import com.earthworm.model.StudyGroup;
import com.earthworm.repository.StudyGroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class StudyGroupService {
    private final StudyGroupRepository repository;

    public StudyGroupService(StudyGroupRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> listGroups() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toPublicMap).toList();
    }

    @Transactional
    public Map<String, Object> createGroup(String userId, String name, String description) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank() || normalizedName.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name must be between 1 and 100 characters.");
        }
        String normalizedDescription = description == null ? "" : description.trim();
        if (normalizedDescription.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group description is too long.");
        }
        StudyGroup g = new StudyGroup();
        g.setId(UUID.randomUUID().toString());
        g.setName(normalizedName);
        g.setDescription(normalizedDescription);
        g.setCreatorId(userId);
        g.setMemberCount(1);
        g.setInviteCode(UUID.randomUUID().toString().substring(0, 8));
        repository.save(g);
        return toOwnedMap(g);
    }

    public Map<String, Object> getGroup(String id, String viewerId) {
        return repository.findById(id)
                .map(group -> Objects.equals(group.getCreatorId(), viewerId) ? toOwnedMap(group) : toPublicMap(group))
                .orElse(Map.of());
    }

    public List<Map<String, Object>> getUserGroups(String userId) {
        return repository.findByCreatorId(userId).stream().map(this::toOwnedMap).toList();
    }

    private Map<String, Object> toPublicMap(StudyGroup g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("name", g.getName());
        m.put("description", g.getDescription());
        m.put("cover", g.getCover());
        m.put("creatorId", g.getCreatorId());
        m.put("memberCount", g.getMemberCount());
        m.put("createdAt", g.getCreatedAt() == null ? null : g.getCreatedAt().toString());
        return m;
    }

    private Map<String, Object> toOwnedMap(StudyGroup g) {
        Map<String, Object> m = toPublicMap(g);
        m.put("inviteCode", g.getInviteCode());
        return m;
    }
}
