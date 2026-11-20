package com.example.luyentap.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {
    @Value("${gemini.api.key}")
    private String apiKey;

    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter chatStream(@RequestBody Map<String, String> payload) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        String userPrompt = payload.get("message");

        if (userPrompt == null || userPrompt.isBlank()) {
            emitter.complete();
            return emitter;
        }

        // Endpoint hỗ trợ Streaming dạng SSE
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:streamGenerateContent?alt=sse&key=" + apiKey;

        // Prompt tối giản để AI xử lý siêu tốc
        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", "You are an English tutor. ONLY answer English learning topics in 1-2 short sentences. If unrelated, refuse in English."))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", userPrompt)))
                )
        );

        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.execute(geminiUrl, org.springframework.http.HttpMethod.POST,
                        request -> {
                            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                            request.getBody().write(json.getBytes(StandardCharsets.UTF_8));
                        },
                        response -> {
                            try (InputStream is = response.getBody()) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                                    emitter.send(chunk);
                                }
                            }
                            emitter.complete();
                            return null;
                        }
                );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}