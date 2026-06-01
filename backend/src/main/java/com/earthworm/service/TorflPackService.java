package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.*;

/**
 * 基于内置 TORFL (ТРКИ) 种子词表生成等级课程包。
 * 每个等级 (A1-C2) 产出：
 *   - 多节单词卡课程（每课 10 词，看中文输入俄语）
 *   - 多节例句课程（看中文输入俄语整句）
 * 词表来自 backend/src/main/resources/torfl/seed.json，是 ТРКИ Лексический Минимум 范围内的精选高频词。
 * 可在显式开启初始化开关时 seed 缺失的等级包。
 */
@Service
public class TorflPackService {
    private static final Logger log = LoggerFactory.getLogger(TorflPackService.class);
    private static final List<String> LEVEL_ORDER = List.of("A1", "A2", "B1", "B2", "C1", "C2");
    private static final Set<String> LEVELS = Set.copyOf(LEVEL_ORDER);

    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${seed.write-on-startup:false}")
    private boolean writeOnStartup;

    private Map<String, LevelSeed> seedCache;

    public TorflPackService(CoursePackRepository coursePackRepository,
                            CourseRepository courseRepository,
                            StatementRepository statementRepository) {
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
    }

    /**
     * 仅在显式初始化空库时 seed 缺失的等级包，避免普通重启修改业务数据。
     */
    @PostConstruct
    public void bootstrap() {
        if (!writeOnStartup) {
            log.info("[torfl] startup seed writes disabled");
            return;
        }
        try {
            for (String level : LEVEL_ORDER) {
                if (!packExistsForLevel(level)) {
                    log.info("[torfl] bootstrap creating pack for level {}", level);
                    createPackFromSeed(level);
                }
            }
        } catch (Exception e) {
            log.warn("[torfl] bootstrap failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 兼容旧接口：POST /admin/torfl-pack/generate {"level":"A1", ...}
     * 若指定等级已有包则直接返回现有包信息；否则按种子创建。
     */
    @Transactional
    public Map<String, Object> generate(Map<String, Object> body) {
        String level = stringValue(body.get("level"), "").toUpperCase(Locale.ROOT);
        if (!LEVELS.contains(level)) {
            throw new IllegalArgumentException("level must be one of A1/A2/B1/B2/C1/C2");
        }

        Optional<CoursePack> existing = findFirstPackForLevel(level);
        if (existing.isPresent()) {
            return summarize(existing.get());
        }
        CoursePack pack = createPackFromSeed(level);
        return summarize(pack);
    }

    @Transactional
    protected CoursePack createPackFromSeed(String level) {
        LevelSeed seed = loadSeed().get(level);
        if (seed == null) {
            throw new IllegalStateException("seed for level " + level + " not found");
        }
        if (seed.words == null || seed.words.isEmpty()) {
            throw new IllegalStateException("seed for level " + level + " has no words");
        }

        CoursePack pack = new CoursePack();
        pack.setId(packId(level));
        pack.setTitle(seed.title != null ? seed.title : "TORFL " + level + " 核心词汇与例句");
        pack.setDescription(seed.description != null ? seed.description :
                "TORFL " + level + " 等级核心词汇练习，含单词卡和例句两类课程。");
        pack.setIsFree(true);
        pack.setCover(coverFor(level));
        pack.setCreatorId("system");
        pack.setShareLevel("public");
        pack.setOrder(nextPackOrder());
        coursePackRepository.save(pack);

        int courseOrder = 1;
        int chunk = 10;

        // 单词卡课程
        for (int i = 0; i < seed.words.size(); i += chunk) {
            List<WordItem> slice = seed.words.subList(i, Math.min(i + chunk, seed.words.size()));
            Course course = new Course();
            course.setId(packId(level) + "-w" + courseOrder);
            course.setTitle("第 " + courseOrder + " 课：核心单词 " + (i + 1) + "-" + (i + slice.size()));
            course.setDescription("TORFL " + level + " 高频词。看中文输入俄语单词。");
            course.setOrder(courseOrder);
            course.setCoursePack(pack);
            courseRepository.save(course);

            int stmtOrder = 1;
            for (WordItem word : slice) {
                if (word.russian == null || word.russian.isBlank() ||
                        word.chinese == null || word.chinese.isBlank()) {
                    continue;
                }
                String chineseLabel = (word.pos == null || word.pos.isBlank())
                        ? word.chinese
                        : word.chinese + "（" + word.pos + "）";
                Statement statement = new Statement();
                statement.setId(course.getId() + "-s" + stmtOrder);
                statement.setOrder(stmtOrder++);
                statement.setEnglish(word.russian);
                statement.setChinese(chineseLabel);
                statement.setSoundmark("");
                statement.setCourse(course);
                statementRepository.save(statement);
            }
            courseOrder++;
        }

        // 例句课程
        if (seed.sentences != null && !seed.sentences.isEmpty()) {
            for (int i = 0; i < seed.sentences.size(); i += chunk) {
                List<SentenceItem> slice = seed.sentences.subList(i, Math.min(i + chunk, seed.sentences.size()));
                Course course = new Course();
                course.setId(packId(level) + "-s" + courseOrder);
                course.setTitle("第 " + courseOrder + " 课：组词成句 " + (i + 1) + "-" + (i + slice.size()));
                course.setDescription("用学过的核心词组成完整句子。看中文输入俄语整句。");
                course.setOrder(courseOrder);
                course.setCoursePack(pack);
                courseRepository.save(course);

                int stmtOrder = 1;
                for (SentenceItem sentence : slice) {
                    if (sentence.russian == null || sentence.russian.isBlank() ||
                            sentence.chinese == null || sentence.chinese.isBlank()) {
                        continue;
                    }
                    Statement statement = new Statement();
                    statement.setId(course.getId() + "-s" + stmtOrder);
                    statement.setOrder(stmtOrder++);
                    statement.setEnglish(sentence.russian);
                    statement.setChinese(sentence.chinese);
                    statement.setSoundmark("");
                    statement.setCourse(course);
                    statementRepository.save(statement);
                }
                courseOrder++;
            }
        }
        log.info("[torfl] created pack {} ({}) with {} courses",
                pack.getId(), pack.getTitle(), courseOrder - 1);
        return pack;
    }

    private boolean packExistsForLevel(String level) {
        String stableId = packId(level);
        String prefix = stableId + "-";
        return coursePackRepository.findAll().stream()
                .anyMatch(p -> stableId.equals(p.getId())
                        || p.getId() != null && p.getId().startsWith(prefix));
    }

    private Optional<CoursePack> findFirstPackForLevel(String level) {
        String stableId = packId(level);
        String prefix = stableId + "-";
        return coursePackRepository.findAll().stream()
                .filter(p -> stableId.equals(p.getId())
                        || p.getId() != null && p.getId().startsWith(prefix))
                .findFirst();
    }

    private Map<String, Object> summarize(CoursePack pack) {
        List<Course> courses = courseRepository.findByCoursePackIdOrderByOrderAsc(pack.getId());
        int wordCount = 0;
        int sentenceCount = 0;
        for (Course course : courses) {
            int sz = statementRepository.findByCourseIdOrderByOrderAsc(course.getId()).size();
            if (course.getId() != null && course.getId().contains("-s")) {
                sentenceCount += sz;
            } else {
                wordCount += sz;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("coursePackId", pack.getId());
        result.put("title", pack.getTitle());
        result.put("courseCount", courses.size());
        result.put("wordCount", wordCount);
        result.put("sentenceCount", sentenceCount);
        return result;
    }

    private synchronized Map<String, LevelSeed> loadSeed() {
        if (seedCache != null) {
            return seedCache;
        }
        Map<String, LevelSeed> result = new LinkedHashMap<>();
        // 优先按等级分别加载 torfl/levels/<LEVEL>.json
        for (String level : LEVEL_ORDER) {
            String path = "torfl/levels/" + level + ".json";
            try (InputStream is = new ClassPathResource(path).getInputStream()) {
                LevelSeed seed = objectMapper.readValue(is, LevelSeed.class);
                result.put(level, seed);
            } catch (Exception e) {
                log.warn("[torfl] failed to load {} ({}), skipping", path, e.getMessage());
            }
        }
        // 兜底：如果 levels 目录全部缺失则回退到旧的 seed.json
        if (result.isEmpty()) {
            try (InputStream is = new ClassPathResource("torfl/seed.json").getInputStream()) {
                result = objectMapper.readValue(is, new TypeReference<Map<String, LevelSeed>>() {});
            } catch (Exception e) {
                throw new IllegalStateException("无法加载 torfl/levels/*.json 或 torfl/seed.json: " + e.getMessage(), e);
            }
        }
        seedCache = result;
        return seedCache;
    }

    /**
     * 重新 seed：删除所有 torfl-* 课程包及其下课程/语句，按当前 seed 重建。
     * 配合修改 JSON 后调用，让前端立刻看到新词表。
     */
    @Transactional
    public Map<String, Object> reseed() {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "TORFL reseed is disabled until existing learning progress can be preserved.");
    }

    private int nextPackOrder() {
        return coursePackRepository.findAll().stream()
                .map(CoursePack::getOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private String coverFor(String level) {
        return switch (level) {
            case "A1", "A2" ->
                    "https://images.unsplash.com/photo-1532153975070-2e9ab71f1b14?q=80&w=1200&auto=format&fit=crop";
            case "B1", "B2" ->
                    "https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1200&auto=format&fit=crop";
            default ->
                    "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1200&auto=format&fit=crop";
        };
    }

    private String stringValue(Object value, String fallback) {
        if (value == null) return fallback;
        String s = value.toString();
        return s.isBlank() ? fallback : s;
    }

    private String packId(String level) {
        return "torfl-" + level.toLowerCase(Locale.ROOT);
    }

    // ---- Jackson-friendly seed shape ----
    public static class LevelSeed {
        public String title;
        public String description;
        public List<WordItem> words;
        public List<SentenceItem> sentences;
    }

    public static class WordItem {
        public String russian;
        public String chinese;
        public String pos;
    }

    public static class SentenceItem {
        public String russian;
        public String chinese;
    }
}
