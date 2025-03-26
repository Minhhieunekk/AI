// ModelAI/src/main/java/com/example/modelai/controller/ImageRestorationController.java
      package com.example.modelai.controller;

      import com.example.modelai.service.ImageRestorationService;
      import lombok.Data;
      import org.springframework.web.bind.annotation.PostMapping;
      import org.springframework.web.bind.annotation.RequestBody;
      import org.springframework.web.bind.annotation.RequestParam;
      import org.springframework.web.bind.annotation.RestController;
      import org.springframework.web.multipart.MultipartFile;

      import java.io.File;

@RestController
      public class ImageRestorationController {
          private final ImageRestorationService service;

          @Data
          public static class TrainRequest {
              private String datasetPath;
          }

          public ImageRestorationController(ImageRestorationService service) {
              this.service = service;
          }

          @PostMapping("/restore")
          public String restoreImage(@RequestParam("image") MultipartFile file) {
              try {
                  return service.restoreImage(file);
              } catch (Exception e) {
                  return "Error restoring image: " + e.getMessage();
              }
          }


          @PostMapping("/train")
          public String trainModel(@RequestBody TrainRequest request) {
              try {
                  String normalizedPath = request.getDatasetPath().replace("\\", "/");
                  File datasetDir = new File(normalizedPath);
                  if (!datasetDir.exists() || !datasetDir.isDirectory()) {
                      throw new IllegalArgumentException("Invalid dataset path");
                  }
                  return service.trainModel(normalizedPath);
              } catch (Exception e) {
                  return "Error training model: " + e.getMessage();
              }
          }
      }