package com.example.aiassistant.controller;

import com.example.aiassistant.model.DamageResponse;
import com.example.aiassistant.service.AnthropicVisionService;
import com.example.aiassistant.service.ImageProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DamageController {

    private final ImageProcessingService imageProcessingService;
    private final AnthropicVisionService anthropicVisionService;

    public DamageController(ImageProcessingService imageProcessingService,
                            AnthropicVisionService anthropicVisionService) {
        this.imageProcessingService = imageProcessingService;
        this.anthropicVisionService = anthropicVisionService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<DamageResponse> analyze(
            @RequestParam("image") MultipartFile image) {
        try {
            System.out.println("Received image: " + image.getOriginalFilename() +
                    " (" + image.getSize() / 1024 + " KB)");

            // Step 1: Resize + crop with OpenCV
            String base64Image = imageProcessingService.processImage(image);

            // Step 2: Send to Anthropic vision model
            DamageResponse result = anthropicVisionService.analyzeDamage(base64Image);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("Controller error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new DamageResponse("error", "error",
                            e.getMessage(), false, "0"));
        }
    }
}