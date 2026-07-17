package com.example.demo.bonus;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Recherche la disponibilité d'un livre à partir de son ISBN.
 *
 * <p>Stratégie : interroger Open Library en premier (Option A) ; si le livre n'y figure pas,
 * basculer sur Google Books ; si aucune des deux sources ne le trouve, retourner un résultat
 * "introuvable" explicite plutôt qu'une erreur technique.
 */
@Service
public class LivreLookupService {

  private static final Pattern ISBN_PATTERN = Pattern.compile("^(97[89])?\\d{9}(\\d|X)$");

  private static final String OPEN_LIBRARY_SEARCH_URL = "https://openlibrary.org/search.json?isbn={isbn}";
  private static final String GOOGLE_BOOKS_URL =
      "https://www.googleapis.com/books/v1/volumes?q=isbn:{isbn}";

  private final RestTemplate restTemplate;

  public LivreLookupService() {
    this.restTemplate = new RestTemplate();
  }

  /** Constructeur utilisé par les tests pour injecter un RestTemplate mocké. */
  LivreLookupService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public LivreDisponibilite rechercherParIsbn(String isbnBrut) {
    String isbn = normaliser(isbnBrut);
    if (!ISBN_PATTERN.matcher(isbn).matches()) {
      throw new IsbnInvalideException(isbnBrut);
    }

    return rechercherSurOpenLibrary(isbn)
        .or(() -> rechercherSurGoogleBooks(isbn))
        .orElseGet(() -> LivreDisponibilite.introuvable(isbn));
  }

  private String normaliser(String isbn) {
    return isbn == null ? "" : isbn.replace("-", "").replace(" ", "").toUpperCase();
  }

  private Optional<LivreDisponibilite> rechercherSurOpenLibrary(String isbn) {
    try {
      JsonNode body = restTemplate.getForObject(OPEN_LIBRARY_SEARCH_URL, JsonNode.class, isbn);
      if (body == null) {
        return Optional.empty();
      }

      JsonNode docs = body.path("docs");
      if (!docs.isArray() || docs.isEmpty()) {
        return Optional.empty();
      }

      JsonNode premierResultat = docs.get(0);
      String titre = premierResultat.path("title").asText(null);
      List<String> auteurs = extraireListe(premierResultat.path("author_name"));

      return Optional.of(LivreDisponibilite.disponible(isbn, "Open Library", titre, auteurs));
    } catch (RestClientException e) {
      // En cas d'erreur réseau/technique sur Open Library, on bascule silencieusement
      // sur Google Books plutôt que de faire échouer toute la requête.
      return Optional.empty();
    }
  }

  private Optional<LivreDisponibilite> rechercherSurGoogleBooks(String isbn) {
    try {
      JsonNode body = restTemplate.getForObject(GOOGLE_BOOKS_URL, JsonNode.class, isbn);
      if (body == null) {
        return Optional.empty();
      }

      int totalItems = body.path("totalItems").asInt(0);
      if (totalItems <= 0) {
        return Optional.empty();
      }

      JsonNode volumeInfo = body.path("items").get(0).path("volumeInfo");
      String titre = volumeInfo.path("title").asText(null);
      List<String> auteurs = extraireListe(volumeInfo.path("authors"));

      return Optional.of(LivreDisponibilite.disponible(isbn, "Google Books", titre, auteurs));
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  private List<String> extraireListe(JsonNode node) {
    if (!node.isArray()) {
      return List.of();
    }
    List<String> resultat = new ArrayList<>();
    node.forEach(element -> resultat.add(element.asText()));
    return resultat;
  }
}
