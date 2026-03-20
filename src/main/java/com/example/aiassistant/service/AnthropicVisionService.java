package com.example.aiassistant.service;

import com.example.aiassistant.model.DamageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AnthropicVisionService {

    private final RestTemplate restTemplate;

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.model}")
    private String model;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    // Focused prompt — limits output tokens dramatically
    private static final String SYSTEM_PROMPT =
            "You are a car damage assessment expert. Be concise. " +
                    "Respond ONLY in this exact JSON format with no extra text:\n" +
                    "{\"location\": \"<part>\", \"severity\": \"<none|minor|moderate|severe>\", " +
                    "\"description\": \"<max 15 words>\", \"damaged\": <true|false>}";

    public AnthropicVisionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DamageResponse analyzeDamage(String base64Image) {
        System.out.println("Calling Anthropic vision API...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        // Build image content block
        Map<String, Object> imageSource = new HashMap<>();
        imageSource.put("type", "base64");
        imageSource.put("media_type", "image/jpeg");
        imageSource.put("data", base64Image);

        Map<String, Object> imageBlock = new HashMap<>();
        imageBlock.put("type", "image");
        imageBlock.put("source", imageSource);

        // Short, focused text prompt — limits output tokens
        Map<String, Object> textBlock = new HashMap<>();
        textBlock.put("type", "text");
        textBlock.put("text", "Analyze this car image for damage. Reply in JSON only.");

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", List.of(imageBlock, textBlock));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 150);   // strict limit — keeps output short
        requestBody.put("system", SYSTEM_PROMPT);
        requestBody.put("messages", List.of(message));

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, entity, Map.class
            );

            // Parse response
            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) response.getBody().get("content");
            String rawText = (String) content.get(0).get("text");
            System.out.println("Raw API response: " + rawText);

            // Parse usage for logging
            Map<String, Object> usage = (Map<String, Object>) response.getBody().get("usage");
            String tokensUsed = "input: " + usage.get("input_tokens") +
                    ", output: " + usage.get("output_tokens");
            System.out.println("Tokens used: " + tokensUsed);

            return parseResponse(rawText, tokensUsed);

        } catch (Exception e) {
            System.out.println("Vision API error: " + e.getMessage());
            return new DamageResponse("unknown", "unknown",
                    "Error analyzing image: " + e.getMessage(), false, "0");
        }
    }

    private DamageResponse parseResponse(String rawText, String tokensUsed) {
        try {
            // Simple JSON parsing without extra library
            String location = extractJson(rawText, "location");
            String severity = extractJson(rawText, "severity");
            String description = extractJson(rawText, "description");
            boolean damaged = rawText.contains("\"damaged\": true") ||
                    rawText.contains("\"damaged\":true");

            return new DamageResponse(location, severity, description, damaged, tokensUsed);
        } catch (Exception e) {
            return new DamageResponse("unknown", "unknown", rawText, true, tokensUsed);
        }
    }

    private String extractJson(String json, String key) {
        String search = "\"" + key + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\":\"";
            start = json.indexOf(search);
        }
        if (start == -1) return "unknown";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "unknown" : json.substring(start, end);
    }
}