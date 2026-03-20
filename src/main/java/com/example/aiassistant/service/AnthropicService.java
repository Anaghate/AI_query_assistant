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
        System.out.println("=== Calling Anthropic API ===");
        System.out.println("Question: " + userQuestion);
        System.out.println("API Key present: " + (apiKey != null && !apiKey.isEmpty()));
        System.out.println("API Key first 10 chars: " + (apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "TOO SHORT"));
        System.out.println("Model: " + model);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userQuestion);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 1024);
            requestBody.put("messages", List.of(message));

            System.out.println("Sending request to Anthropic...");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, entity, Map.class
            );

            System.out.println("=== Got response! Status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());

            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) response.getBody().get("content");
            String answer = (String) content.get(0).get("text");
            System.out.println("Answer: " + answer);
            return answer;

        } catch (Exception e) {
            System.out.println("=== ERROR ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}