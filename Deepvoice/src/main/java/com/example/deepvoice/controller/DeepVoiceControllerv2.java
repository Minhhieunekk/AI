package com.example.deepvoice.controller;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@RestController
@RequestMapping("/api/deep-voicev2")
public class DeepVoiceControllerv2 {

    @Value("${replicate.api.key}")
    private String replicateApiKey;

    private static final String REPLICATE_URL = "https://api.replicate.com/v1/predictions";

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestParam("file") MultipartFile file) {
        try {
            // Lưu file tạm
            File tempFile = File.createTempFile("audio", ".wav");
            file.transferTo(tempFile);

            // Chuyển file thành base64
            byte[] fileContent = Files.readAllBytes(tempFile.toPath());
            String base64Audio = Base64.getEncoder().encodeToString(fileContent);

            // Gửi request đến Replicate API
            String transcription = sendToReplicate(base64Audio);

            // Xóa file tạm
            tempFile.delete();

            return new ResponseEntity<>(transcription, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error processing the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String sendToReplicate(String base64Audio) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(REPLICATE_URL);
            request.setHeader("Authorization", "Token " + replicateApiKey);
            request.setHeader("Content-Type", "application/json");

            // Body request gửi đến Replicate API
            String jsonBody = "{"
                    + "\"version\": \"8099696689d249cf8b122d833c36ac3f75505c666a395ca40ef26f68e7d3d16e\","
                    + "\"input\": { \"audio\": \"data:audio/wav;base64," + base64Audio + "\" }"
                    + "}";

            request.setEntity(new StringEntity(jsonBody));

            // Gửi request và nhận phản hồi
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                System.out.println(jsonResponse);

                return jsonResponse.path("output").asText("Transcription failed");
            }
        }
    }
}
