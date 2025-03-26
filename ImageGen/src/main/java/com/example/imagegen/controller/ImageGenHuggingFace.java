package com.example.imagegen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api/hugging-face")
public class ImageGenHuggingFace {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

   @GetMapping("/generate")
    public ResponseEntity<byte[]> generateImage(@RequestParam String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.IMAGE_PNG));
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> request = new HttpEntity<>(String.format("{\"inputs\": \"%s\"}", prompt), headers);
        String API_URL = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-2";

        int maxRetries = 5;
        int retryDelayMs = 2000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        API_URL,
                        HttpMethod.POST,
                        request,
                        byte[].class
                );

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.IMAGE_PNG);
                return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE && i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    continue;
                }
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
