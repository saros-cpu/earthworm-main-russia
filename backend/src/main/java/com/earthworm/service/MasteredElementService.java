package com.earthworm.service;
import com.earthworm.config.UserContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.earthworm.model.MasteredElement;
import com.earthworm.repository.MasteredElementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MasteredElementService {
    private static final int MAX_CONTENT_LENGTH = 4000;
    private final MasteredElementRepository repository;
    private final ObjectMapper objectMapper;

    public MasteredElementService(MasteredElementRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> findAll() {
        return repository.findByUserIdOrderByMasteredAtDesc(UserContext.getUserId())
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Transactional
    public Map<String, Object> add(Map<String, Object> body) {
        Object content = body.get("content");
        MasteredElement element = new MasteredElement();
        element.setId(UUID.randomUUID().toString());
        element.setUserId(UserContext.getUserId());
        element.setContent(toJson(content));
        repository.save(element);
        return toItem(element);
    }

    @Transactional
    public Boolean remove(String id) {
        return repository.findByIdAndUserId(id, UserContext.getUserId())
                .map(element -> {
                    repository.delete(element);
                    return true;
                })
                .orElse(false);
    }

    private Map<String, Object> toItem(MasteredElement element) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", element.getId());
        item.put("content", parseContent(element.getContent()));
        item.put(
                "masteredAt",
                element.getMasteredAt() == null
                        ? null
                        : element.getMasteredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        return item;
    }

    private Map<String, Object> parseContent(String content) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (content == null) {
            result.put("english", "");
            result.put("targetText", "");
            return result;
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(content, new TypeReference<>() {});
            String targetText = Objects.toString(
                    parsed.getOrDefault("targetText", parsed.getOrDefault("english", "")),
                    ""
            );
            result.put("english", targetText);
            result.put("targetText", targetText);
            return result;
        } catch (JsonProcessingException ignored) {
            // Keep reading legacy plain-text content if an old row was not stored as JSON.
        }
        result.put("english", content);
        result.put("targetText", content);
        return result;
    }

    private String toJson(Object content) {
        String value;
        if (content instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) content;
            Object targetText = map.get("targetText");
            Object english = map.get("english");
            value = targetText == null ? (english == null ? "" : english.toString()) : targetText.toString();
        } else {
            value = content == null ? "" : content.toString();
        }
        if (value.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Mastered element content is too long");
        }
        try {
            return objectMapper.writeValueAsString(Map.of("english", value, "targetText", value));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to store mastered element", exception);
        }
    }
}

