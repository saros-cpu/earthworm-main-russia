package com.earthworm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class PdfImportController {
    @PostMapping("/course-pack/import/pdf")
    public Map<String, Object> importPdf(@RequestPart("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        throw unavailable();
    }

    @PostMapping("/course-pack/import/pdf/jobs")
    public Map<String, Object> createImportJob(@RequestPart("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        throw unavailable();
    }

    @GetMapping("/course-pack/import/pdf/jobs/{jobId}")
    public Map<String, Object> getImportJob(@PathVariable("jobId") String jobId) {
        throw unavailable();
    }

    @GetMapping("/admin/pdf-import-jobs")
    public Object listImportJobs(@RequestParam(value = "limit", defaultValue = "20") int limit) {
        throw unavailable();
    }

    @PostMapping("/admin/pdf-import-jobs/local-directory")
    public Map<String, Object> createLocalDirectoryImportJobs(@RequestBody Map<String, Object> body) {
        throw unavailable();
    }

    @DeleteMapping("/admin/pdf-import-jobs/{jobId}")
    public Boolean deleteJob(@PathVariable("jobId") String jobId) {
        throw unavailable();
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "PDF import is not available yet.");
    }
}
