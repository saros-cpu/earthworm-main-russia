package com.earthworm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OpenAiRefinementClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.baseUrl:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    public Map<String, Object> generateCourse(String topic, String level, int count) {
        String prompt = "Generate a Russian language course about " + topic + " at " + level + " level. Return JSON with 'title', 'description', and 'items' array. Each item has 'sourceText' (Chinese), 'targetText' (Russian), 'translation' (Chinese), 'phonetic'.";

        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return generateFallbackCourse(topic, level, count);
        }

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<?, ?> result = objectMapper.readValue(response.body(), Map.class);
                Object choices = result.get("choices");
                if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                    String content = (String) ((Map<?, ?>) ((Map<?, ?>) ((List<?>) choices).get(0)).get("message")).get("content");
                    try {
                        return objectMapper.readValue(extractJson(content), Map.class);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return generateFallbackCourse(topic, level, count);
    }

    private Map<String, Object> generateFallbackCourse(String topic, String level, int count) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", "Course: " + topic);
        result.put("description", "Study " + topic + " at " + level + " level");
        List<Map<String, String>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("sourceText", "Example " + (i + 1));
            item.put("targetText", "Example text " + (i + 1));
            item.put("translation", "Example " + (i + 1));
            item.put("phonetic", "");
            items.add(item);
        }
        result.put("items", items);
        return result;
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }
}
