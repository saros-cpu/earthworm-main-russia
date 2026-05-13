package com.earthworm.service;

import com.earthworm.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class PdfImportService {
    public String uploadPdf(MultipartFile file, String title) {
        return "pdf-job-" + UUID.randomUUID();
    }

    public Map<String, Object> createLocalImportJobs(String directory, boolean recursive) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("directory", directory);
        result.put("foundCount", 0);
        result.put("createdCount", 0);
        result.put("skippedCount", 0);
        result.put("jobs", List.of());
        result.put("skipped", List.of());
        return result;
    }
}
