package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加载 backend/src/main/resources/customs/*.json 中的预编排课程包数据
 * （如 baby_care.json、oil_engineering.json）并入库。
 * 文件 schema 见 scripts/custom-import/build_*.py 输出。
 */
@Service
public class CustomCoursePackService {
    private static final Logger log = LoggerFactory.getLogger(CustomCoursePackService.class);
    private static final String RESOURCE_PATTERN = "classpath:customs/*.json";
    public static final Map<String, String> LYRICS_CACHE = new ConcurrentHashMap<>();

    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomCoursePackService(CoursePackRepository coursePackRepository,
                                   CourseRepository courseRepository,
                                   StatementRepository statementRepository) {
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
    }

    @PostConstruct
    public void bootstrap() {
        try {
            for (JsonNode pack : loadPackResources()) {
                String packId = pack.path("id").asText();
                if (packId.isBlank()) {
                    log.warn("[custom] skip pack without id");
                    continue;
                }
                if (coursePackRepository.findById(packId).isPresent()) {
                    syncVideoPaths(pack);
                    continue;
                }
                createPack(pack);
            }
        } catch (Exception e) {
            log.warn("[custom] bootstrap failed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public Map<String, Object> reseed() {
        List<Map<String, Object>> created = new ArrayList<>();
        int deleted = 0;
        try {
            List<JsonNode> packs = loadPackResources();
            // Delete existing packs first
            for (JsonNode pack : packs) {
                String packId = pack.path("id").asText();
                if (packId.isBlank()) continue;
                Optional<CoursePack> existing = coursePackRepository.findById(packId);
                if (existing.isPresent()) {
                    List<Course> oldCourses = courseRepository.findByCoursePackIdOrderByOrderAsc(packId);
                    for (Course c : oldCourses) {
                        List<Statement> stmts = statementRepository.findByCourseIdOrderByOrderAsc(c.getId());
                        for (Statement s : stmts) statementRepository.delete(s);
                        courseRepository.delete(c);
                    }
                    coursePackRepository.delete(existing.get());
                    deleted++;
                }
            }
            for (JsonNode pack : packs) {
                CoursePack saved = createPack(pack);
                if (saved == null) continue;
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("id", saved.getId());
                summary.put("title", saved.getTitle());
                int courseCount = pack.path("courses").size();
                int stmtCount = 0;
                for (JsonNode c : pack.path("courses")) stmtCount += c.path("statements").size();
                summary.put("courseCount", courseCount);
                summary.put("statementCount", stmtCount);
                created.add(summary);
            }
        } catch (Exception e) {
            log.error("[custom] reseed failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deleted", deleted);
        result.put("created", created);
        return result;
    }

    private List<JsonNode> loadPackResources() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources = resolver.getResources(RESOURCE_PATTERN);
        List<JsonNode> packs = new ArrayList<>();
        for (Resource res : resources) {
            try (InputStream is = res.getInputStream()) {
                JsonNode node = objectMapper.readTree(is);
                packs.add(node);
            } catch (Exception e) {
                log.warn("[custom] failed to read {}: {}", res.getFilename(), e.getMessage());
            }
        }
        return packs;
    }

    @Transactional
    protected CoursePack createPack(JsonNode pack) {
        String packId = pack.path("id").asText();
        if (packId.isBlank()) return null;
        if (coursePackRepository.findById(packId).isPresent()) {
            log.info("[custom] pack {} already exists, skip create", packId);
            return null;
        }
        CoursePack cp = new CoursePack();
        cp.setId(packId);
        cp.setTitle(pack.path("title").asText("(未命名课程包)"));
        cp.setDescription(pack.path("description").asText(""));
        cp.setCover(pack.path("cover").asText(""));
        cp.setIsFree(pack.path("isFree").asBoolean(true));
        cp.setCreatorId(pack.path("creatorId").asText("system"));
        cp.setShareLevel(pack.path("shareLevel").asText("PUBLIC"));
        cp.setOrder(nextPackOrder());
        cp.setCreatedAt(LocalDateTime.now());
        cp.setUpdatedAt(LocalDateTime.now());
        coursePackRepository.save(cp);

        JsonNode coursesNode = pack.path("courses");
        int courseOrder = 0;
        int totalStmts = 0;
        for (JsonNode courseNode : coursesNode) {
            courseOrder++;
            String courseId = courseNode.path("id").asText();
            if (courseId.isBlank()) {
                courseId = packId + "-c" + courseOrder + "-" + shortRandom();
            }
            Course c = new Course();
            c.setId(courseId);
            c.setCoursePack(cp);
            c.setOrder(courseNode.path("order").asInt(courseOrder));
            c.setTitle(courseNode.path("title").asText("第 " + courseOrder + " 课"));
            c.setDescription(courseNode.path("description").asText(""));
            c.setVideo(courseNode.path("video").asText(""));
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(c);

            syncLyricsCache(courseId, courseNode);

            int stmtOrder = 0;
            for (JsonNode s : courseNode.path("statements")) {
                stmtOrder++;
                String stmtId = courseId + "-s" + stmtOrder + "-" + shortRandom();
                Statement st = new Statement();
                st.setId(stmtId);
                st.setOrder(stmtOrder);
                st.setEnglish(s.path("english").asText(""));
                st.setChinese(s.path("chinese").asText(""));
                st.setSoundmark(s.path("soundmark").asText(""));
                st.setCourse(c);
                st.setCreatedAt(LocalDateTime.now());
                st.setUpdatedAt(LocalDateTime.now());
                statementRepository.save(st);
                totalStmts++;
            }
        }
        log.info("[custom] seeded pack {} ({}, {} courses, {} statements)", packId, cp.getTitle(), courseOrder, totalStmts);
        return cp;
    }

    private void syncVideoPaths(JsonNode pack) {
        String packId = pack.path("id").asText();
        List<Course> courses = courseRepository.findByCoursePackIdOrderByOrderAsc(packId);
        for (Course course : courses) {
            for (JsonNode cn : pack.path("courses")) {
                if (cn.path("id").asText("").equals(course.getId())) {
                    String jsonVideo = cn.path("video").asText("");
                    if (!jsonVideo.isEmpty() && !jsonVideo.equals(course.getVideo())) {
                        course.setVideo(jsonVideo);
                        courseRepository.save(course);
                        log.info("[custom] updated video for {}: {}", course.getId(), jsonVideo);
                    }
                    syncLyricsCache(course.getId(), cn);
                    break;
                }
            }
        }
    }

    private void syncLyricsCache(String courseId, JsonNode courseNode) {
        JsonNode lyrics = courseNode.path("lyrics");
        if (lyrics.isArray() && lyrics.size() > 0) {
            LYRICS_CACHE.put(courseId, lyrics.toString());
        } else {
            LYRICS_CACHE.remove(courseId);
        }
    }

    private int nextPackOrder() {
        return coursePackRepository.findAll().stream()
                .map(CoursePack::getOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private String shortRandom() {
        int n = ThreadLocalRandom.current().nextInt(0x10000000, 0x7fffffff);
        return Integer.toHexString(n);
    }
}
