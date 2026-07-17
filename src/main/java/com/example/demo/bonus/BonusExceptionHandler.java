package com.example.demo.bonus;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Transforme les erreurs de validation d'ISBN du package bonus en réponses HTTP 400 propres. */
@RestControllerAdvice(basePackageClasses = LivreController.class)
public class BonusExceptionHandler {

  @ExceptionHandler(IsbnInvalideException.class)
  public ResponseEntity<Map<String, String>> gererIsbnInvalide(IsbnInvalideException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", e.getMessage()));
  }
}
