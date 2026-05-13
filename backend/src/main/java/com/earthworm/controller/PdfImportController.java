package com.earthworm.controller;

import com.earthworm.service.PdfImportService;
import com.earthworm.service.PdfImportJobService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class PdfImportController {
    private final PdfImportService pdfImportService;
    private final PdfImportJobService pdfImportJobService;

    public PdfImportController(PdfImportService pdfImportService, PdfImportJobService pdfImportJobService) {
        this.pdfImportService = pdfImportService;
        this.pdfImportJobService = pdfImportJobService;
    }

    @PostMapping("/course-pack/import/pdf")
    public Map<String, Object> importPdf(@RequestPart("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        String jobId = pdfImportService.uploadPdf(file, title);
        return Map.of("jobId", jobId, "status", "queued");
    }

    @PostMapping("/course-pack/import/pdf/jobs")
    public Map<String, Object> createImportJob(@RequestPart("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        String jobId = pdfImportService.uploadPdf(file, title);
        return Map.of("jobId", jobId, "status", "queued");
    }

    @GetMapping("/course-pack/import/pdf/jobs/{jobId}")
    public Map<String, Object> getImportJob(@PathVariable("jobId") String jobId) {
        return Map.of("id", jobId, "status", "completed");
    }

    @GetMapping("/admin/pdf-import-jobs")
    public Object listImportJobs(@RequestParam(value = "limit", defaultValue = "20") int limit) {
        return pdfImportJobService.getJobs(limit);
    }

    @PostMapping("/admin/pdf-import-jobs/local-directory")
    public Map<String, Object> createLocalDirectoryImportJobs(@RequestBody Map<String, Object> body) {
        return pdfImportService.createLocalImportJobs((String) body.get("directory"), Boolean.TRUE.equals(body.get("recursive")));
    }

    @DeleteMapping("/admin/pdf-import-jobs/{jobId}")
    public Boolean deleteJob(@PathVariable("jobId") String jobId) {
        return pdfImportJobService.deleteJob(jobId);
    }
}
