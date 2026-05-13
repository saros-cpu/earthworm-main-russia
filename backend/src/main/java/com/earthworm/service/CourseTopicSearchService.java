package com.earthworm.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseTopicSearchService {
    private static final List<TopicSeed> TOPIC_SEEDS = List.of(
        new TopicSeed("Russian Alphabet", "beginner", "alphabet letters pronunciation"),
        new TopicSeed("Basic Greetings", "beginner", "greetings hello goodbye"),
        new TopicSeed("Numbers & Time", "beginner", "numbers counting time date"),
        new TopicSeed("Family & People", "beginner", "family members relatives"),
        new TopicSeed("Food & Drinks", "beginner", "food cafe restaurant menu"),
        new TopicSeed("Shopping", "beginner", "shopping prices numbers"),
        new TopicSeed("Travel Basics", "elementary", "travel city transport directions"),
        new TopicSeed("Hotel & Accommodation", "elementary", "hotel booking room requests"),
        new TopicSeed("Verbs & Grammar", "elementary", "grammar verbs conjugation"),
        new TopicSeed("Daily Routine", "elementary", "daily routine time activities"),
        new TopicSeed("Weather & Seasons", "elementary", "weather seasons nature"),
        new TopicSeed("Health & Body", "intermediate", "health body doctor medicine"),
        new TopicSeed("Work & Business", "intermediate", "work office business meeting"),
        new TopicSeed("Culture & Traditions", "intermediate", "russian culture traditions holidays"),
        new TopicSeed("Advanced Grammar", "advanced", "grammar cases declension complex")
    );

    public List<Map<String, Object>> search(String keyword) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (TopicSeed seed : TOPIC_SEEDS) {
            if (keyword == null || keyword.isBlank() ||
                seed.topic.toLowerCase().contains(keyword.toLowerCase()) ||
                seed.tags.toLowerCase().contains(keyword.toLowerCase())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("topic", seed.topic);
                item.put("level", seed.level);
                item.put("count", 12);
                item.put("score", 1.0);
                item.put("sourceNote", "system");
                results.add(item);
            }
        }
        return results;
    }

    public List<Map<String, Object>> searchOnline(String keyword) {
        return search(keyword);
    }

    private record TopicSeed(String topic, String level, String tags) {}
}
