package com.example.deepvoice.controller;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/assembly-ai")
public class TranscriptionController {
    private static final String ASSEMBLY_AI_API_KEY = "e92bdad10bd14e7783312404d50269a4";
    private static final String ASSEMBLY_AI_UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String ASSEMBLY_AI_TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Upload the file
        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.set("authorization", ASSEMBLY_AI_API_KEY);
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> uploadBody = new LinkedMultiValueMap<>();
        uploadBody.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(uploadBody, uploadHeaders);
        ResponseEntity<Map> uploadResponse = restTemplate.postForEntity(ASSEMBLY_AI_UPLOAD_URL, uploadRequest, Map.class);

        if (uploadResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>("File upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String uploadUrl = (String) uploadResponse.getBody().get("upload_url");

        HttpHeaders transcriptHeaders = new HttpHeaders();
        transcriptHeaders.set("authorization", ASSEMBLY_AI_API_KEY);
        transcriptHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> transcriptBody = Collections.singletonMap("audio_url", uploadUrl);
        HttpEntity<Map<String, String>> transcriptRequest = new HttpEntity<>(transcriptBody, transcriptHeaders);

        ResponseEntity<String> transcriptResponse = restTemplate.postForEntity(ASSEMBLY_AI_TRANSCRIPT_URL, transcriptRequest, String.class);

        return transcriptResponse;
    }
}