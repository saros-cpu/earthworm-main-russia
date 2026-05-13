package com.earthworm.service;
import com.earthworm.config.UserContext;

import com.earthworm.model.MasteredElement;
import com.earthworm.repository.MasteredElementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MasteredElementService {
    private final MasteredElementRepository repository;

    public MasteredElementService(MasteredElementRepository repository) {
        this.repository = repository;
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
        repository.deleteById(id);
        return true;
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
        String marker = "\"english\"";
        int markerIndex = content.indexOf(marker);
        if (markerIndex >= 0) {
            int colon = content.indexOf(':', markerIndex);
            int firstQuote = content.indexOf('"', colon + 1);
            int secondQuote = content.indexOf('"', firstQuote + 1);
            if (firstQuote >= 0 && secondQuote > firstQuote) {
                String targetText = content.substring(firstQuote + 1, secondQuote);
                result.put("english", targetText);
                result.put("targetText", targetText);
                return result;
            }
        }
        result.put("english", content);
        result.put("targetText", content);
        return result;
    }

    private String toJson(Object content) {
        if (content instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) content;
            Object targetText = map.get("targetText");
            Object english = map.get("english");
            String value = targetText == null ? (english == null ? "" : english.toString()) : targetText.toString();
            return "{\"english\":\"" + escapeJson(value) + "\",\"targetText\":\"" + escapeJson(value) + "\"}";
        }
        String value = content == null ? "" : content.toString();
        return "{\"english\":\"" + escapeJson(value) + "\",\"targetText\":\"" + escapeJson(value) + "\"}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

