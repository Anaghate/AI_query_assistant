package com.example.aiassistant.controller;

import com.example.aiassistant.model.ChatRequest;
import com.example.aiassistant.model.ChatResponse;
import com.example.aiassistant.service.AnthropicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // allows the HTML frontend to call this
public class ChatController {

    private final AnthropicService anthropicService;

    public ChatController(AnthropicService anthropicService) {
        this.anthropicService = anthropicService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest request) {
        String answer = anthropicService.askClaude(request.getQuestion());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}