package com.earthworm.service;

import com.earthworm.model.StudyGroup;
import com.earthworm.model.StudyGroupActivity;
import com.earthworm.model.StudyGroupMember;
import com.earthworm.model.User;
import com.earthworm.repository.StudyGroupActivityRepository;
import com.earthworm.repository.StudyGroupMemberRepository;
import com.earthworm.repository.StudyGroupRepository;
import com.earthworm.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class StudyGroupService {
    private final StudyGroupRepository repository;
    private final StudyGroupMemberRepository memberRepository;
    private final StudyGroupActivityRepository activityRepository;
    private final UserRepository userRepository;

    public StudyGroupService(StudyGroupRepository repository,
                             StudyGroupMemberRepository memberRepository,
                             StudyGroupActivityRepository activityRepository,
                             UserRepository userRepository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
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

        StudyGroupMember owner = new StudyGroupMember();
        owner.setId(UUID.randomUUID().toString());
        owner.setGroupId(g.getId());
        owner.setUserId(userId);
        owner.setRole("owner");
        memberRepository.save(owner);

        addActivity(g.getId(), userId, "create", "创建了小组");
        return toOwnedMap(g);
    }

    public Map<String, Object> getGroup(String id, String viewerId) {
        return repository.findById(id)
                .map(group -> Objects.equals(group.getCreatorId(), viewerId) ? toOwnedMap(group) : toPublicMap(group))
                .orElse(Map.of());
    }

    public List<Map<String, Object>> getUserGroups(String userId) {
        List<String> groupIds = memberRepository.findByUserId(userId)
                .stream().map(StudyGroupMember::getGroupId).toList();
        return repository.findAllById(groupIds).stream().map(this::toOwnedMap).toList();
    }

    @Transactional
    public Map<String, Object> joinGroup(String groupId, String userId) {
        StudyGroup group = repository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a member of this group");
        }

        StudyGroupMember member = new StudyGroupMember();
        member.setId(UUID.randomUUID().toString());
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("member");
        memberRepository.save(member);

        group.setMemberCount((int) memberRepository.countByGroupId(groupId));
        repository.save(group);

        addActivity(groupId, userId, "join", "加入了小组");
        return toPublicMap(group);
    }

    @Transactional
    public Map<String, Object> leaveGroup(String groupId, String userId) {
        StudyGroup group = repository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (Objects.equals(group.getCreatorId(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot leave the group. Transfer ownership first.");
        }

        StudyGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not a member of this group"));

        memberRepository.delete(member);
        group.setMemberCount((int) memberRepository.countByGroupId(groupId));
        repository.save(group);

        addActivity(groupId, userId, "leave", "离开了小组");
        return toPublicMap(group);
    }

    public List<Map<String, Object>> getGroupMembers(String groupId) {
        return memberRepository.findByGroupId(groupId).stream().map(m -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("joinedAt", m.getJoinedAt() == null ? null : m.getJoinedAt().toString());
            User user = userRepository.findById(m.getUserId()).orElse(null);
            item.put("username", user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : m.getUserId());
            return item;
        }).toList();
    }

    public List<Map<String, Object>> getGroupActivities(String groupId) {
        return activityRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("userId", a.getUserId());
            item.put("activityType", a.getActivityType());
            item.put("description", a.getDescription());
            item.put("createdAt", a.getCreatedAt() == null ? null : a.getCreatedAt().toString());
            User user = userRepository.findById(a.getUserId()).orElse(null);
            item.put("username", user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : a.getUserId());
            return item;
        }).toList();
    }

    private void addActivity(String groupId, String userId, String type, String description) {
        StudyGroupActivity a = new StudyGroupActivity();
        a.setId(UUID.randomUUID().toString());
        a.setGroupId(groupId);
        a.setUserId(userId);
        a.setActivityType(type);
        a.setDescription(description);
        activityRepository.save(a);
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