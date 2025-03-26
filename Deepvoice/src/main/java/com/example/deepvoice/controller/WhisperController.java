package com.example.deepvoice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/whisper")
@RequiredArgsConstructor
public class WhisperController {

    @Value("${huggingface.api.token}")
    private String apiKey;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        // Chuyển file sang base64
        String base64Audio = Base64.getEncoder().encodeToString(file.getBytes());

        // Gửi request đến Hugging Face API
        String transcription = sendToHuggingFace(file);

        return ResponseEntity.ok(transcription);
    }

    private String sendToHuggingFace(MultipartFile file) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(apiUrl);
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "audio/wav"); // Định dạng file

            // Gửi file nhị phân
            request.setEntity(new ByteArrayEntity(file.getBytes(), ContentType.create("audio/wav")));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                System.out.println(jsonResponse);
                return jsonResponse.path("text").asText("Transcription failed");
            }
        }
    }

}
