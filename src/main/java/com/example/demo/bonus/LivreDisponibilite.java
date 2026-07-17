package com.example.demo.bonus;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Représente le résultat de la recherche d'un livre par ISBN, quelle que soit la source
 * (Open Library en priorité, Google Books en repli).
 */
@Data
@AllArgsConstructor
public class LivreDisponibilite {

  private String isbn;
  private boolean disponible;
  private String source;
  private String titre;
  private List<String> auteurs;
  private String message;

  public static LivreDisponibilite disponible(
      String isbn, String source, String titre, List<String> auteurs) {
    return new LivreDisponibilite(
        isbn, true, source, titre, auteurs, "Livre trouvé sur " + source + ".");
  }

  public static LivreDisponibilite introuvable(String isbn) {
    return new LivreDisponibilite(
        isbn,
        false,
        null,
        null,
        List.of(),
        "Aucun livre correspondant à cet ISBN n'a été trouvé, ni sur Open Library, ni sur"
            + " Google Books.");
  }
}
