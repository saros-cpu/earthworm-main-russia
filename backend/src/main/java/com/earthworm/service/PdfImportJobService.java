package com.earthworm.service;

import com.earthworm.model.CoursePack;
import com.earthworm.repository.CoursePackRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PdfImportJobService {
    private final CoursePackRepository coursePackRepository;

    public PdfImportJobService(CoursePackRepository coursePackRepository) {
        this.coursePackRepository = coursePackRepository;
    }

    public List<Map<String, Object>> getJobs(int limit) {
        return List.of();
    }

    public boolean deleteJob(String jobId) {
        return true;
    }
}
