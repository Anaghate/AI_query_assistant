package com.example.aiassistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AnthropicService {

    private final RestTemplate restTemplate;

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.model}")
    private String model;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    public AnthropicService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String askClaude(String userQuestion) {
        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        // Build message object
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userQuestion);

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1024);
        requestBody.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Call Anthropic API
        ResponseEntity<Map> response = restTemplate.exchange(
                API_URL, HttpMethod.POST, entity, Map.class
        );

        // Parse response: content[0].text
        List<Map<String, Object>> content =
                (List<Map<String, Object>>) response.getBody().get("content");

        return (String) content.get(0).get("text");
    }
}