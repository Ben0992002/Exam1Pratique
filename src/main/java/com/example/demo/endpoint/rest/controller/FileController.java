package com.example.demo.endpoint.rest.controller;

import com.example.demo.PojaGenerated;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@PojaGenerated
public class FileController {

  private final Mailer mailer;
  
  private static final Map<String, ImageJob> database = new ConcurrentHashMap<>();

  @Data
  @AllArgsConstructor
  public static class ImageJob {
    private String id;
    private String fileName;
    private String email;
    private String status;
    private Instant createdAt;
  }

  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> uploadImage(
      @RequestParam("file") MultipartFile multipartFile,
      @RequestParam("email_to") String emailTo) {
    
    String jobId = UUID.randomUUID().toString();
    String fileName = multipartFile.getOriginalFilename();
    
    ImageJob job = new ImageJob(jobId, fileName, emailTo, "PENDING", Instant.now());
    database.put(jobId, job);

    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(2000); 

        String s3Url = "https://bucket.s3.amazonaws.com/images/nb-" + fileName;

        InternetAddress recipient = new InternetAddress(emailTo);
        
        Email email = new Email(
            recipient,
            List.of(),
            List.of(),
            "Confirmation de traitement - Image Noir et Blanc",
            "Votre image " + fileName + " a été convertie en Noir et Blanc et stockée sur S3.<br>Lien d'accès : <a href=\"" + s3Url + "\">" + s3Url + "</a>",
            List.of()
        );
        
        mailer.accept(email);

        job.setStatus("FINISHED");
        database.put(jobId, job);

      } catch (Exception e) {
        job.setStatus("ERROR");
        database.put(jobId, job);
      }
    });

    return ResponseEntity.ok(Map.of("id", jobId, "status", job.getStatus()));
  }

  @GetMapping("/images/{id}")
  public ResponseEntity<?> verifyImageStatus(@PathVariable String id) {
    ImageJob job = database.get(id);
    
    if (job == null) {
      return ResponseEntity.status(404).body(Map.of("error", "Job non trouvé en base de données"));
    }
    
    return ResponseEntity.ok(Map.of(
        "id", job.getId(),
        "fileName", job.getFileName(),
        "email", job.getEmail(),
        "status", job.getStatus(),
        "createdAt", job.getCreatedAt().toString()
    ));
  }

}