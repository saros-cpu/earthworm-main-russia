package com.earthworm.service;

import com.earthworm.model.Statement;
import com.earthworm.repository.StatementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AiAssistantService {
    private final StatementRepository statementRepository;

    @Value("${openai.baseUrl:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    public AiAssistantService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public Map<String, Object> ask(String question, String statementId) {
        String context = "";
        if (statementId != null) {
            Statement stmt = statementRepository.findById(statementId).orElse(null);
            if (stmt != null) {
                context = "Russian sentence: " + stmt.getEnglish() + "\nChinese translation: " + stmt.getChinese();
            }
        }

        String prompt = "You are a Russian grammar assistant. Answer in Chinese. " +
                (context.isEmpty() ? "" : "\n\nCurrent learning context:\n" + context) +
                "\n\nUser question: " + question +
                "\n\nRequirements: concise and accurate, provide tables for grammar changes (declension, conjugation, etc).";

        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("answer", "AI not configured (missing OPENROUTER_API_KEY). Set it in environment variables.");
        }

        try {
            String escaped = jsonEscape(prompt);
            String body = "{\"model\":\"" + jsonEscape(model) + "\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escaped + "\"}]}";

            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            String responseBody;
            InputStream responseStream = status == 200 ? conn.getInputStream() : conn.getErrorStream();
            if (responseStream != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    responseBody = sb.toString();
                }
            } else {
                responseBody = "";
            }

            if (status != 200) {
                String hint;
                if (status == 402) hint = "API Key insufficient balance. Check OpenRouter account.";
                else if (status == 401) hint = "API Key invalid. Check OPENROUTER_API_KEY.";
                else if (status == 429) hint = "Rate limited, try again later.";
                else hint = "HTTP " + status;
                return Map.of("answer", "AI service unavailable (" + hint + "). Check your API key configuration.");
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<?, ?> result = mapper.readValue(responseBody, Map.class);
            Object choicesObj = result.get("choices");
            if (choicesObj instanceof List && !((List<?>) choicesObj).isEmpty()) {
                Map<?, ?> first = (Map<?, ?>) ((List<?>) choicesObj).get(0);
                Map<?, ?> msg = (Map<?, ?>) first.get("message");
                Object content = msg.get("content");
                return Map.of("answer", content != null ? content.toString() : "");
            }
            return Map.of("answer", "AI temporarily unavailable, please try again later.");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Unexpected character")) {
                return Map.of("answer", "AI service returned unexpected response, possible API key issue.");
            }
            return Map.of("answer", "AI request failed: " + e.getClass().getSimpleName() + " - " + (msg != null ? msg : "unknown error"));
        }
    }

    private String jsonEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
