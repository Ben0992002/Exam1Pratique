package com.example.demo.bonus;

/** Levée lorsque l'ISBN fourni en paramètre de route ne respecte pas un format ISBN-10/13 valide. */
public class IsbnInvalideException extends RuntimeException {

  public IsbnInvalideException(String isbn) {
    super("Le format de l'ISBN '" + isbn + "' est invalide.");
  }
}
