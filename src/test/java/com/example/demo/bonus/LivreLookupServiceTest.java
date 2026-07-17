package com.example.demo.bonus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class LivreLookupServiceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private RestTemplate restTemplate;
  private LivreLookupService service;

  @BeforeEach
  void setUp() {
    restTemplate = mock(RestTemplate.class);
    service = new LivreLookupService(restTemplate);
  }

  @Test
  void trouve_le_livre_sur_open_library_en_priorite() throws Exception {
    JsonNode openLibraryBody =
        objectMapper.readTree(
            "{\"docs\": [{\"title\": \"Clean Code\", \"author_name\": [\"Robert C. Martin\"]}]}");

    when(restTemplate.getForObject(
            contains("openlibrary.org"), eq(JsonNode.class), eq("9780132350884")))
        .thenReturn(openLibraryBody);

    LivreDisponibilite resultat = service.rechercherParIsbn("978-0-13-235088-4");

    assertThat(resultat.isDisponible()).isTrue();
    assertThat(resultat.getSource()).isEqualTo("Open Library");
    assertThat(resultat.getTitre()).isEqualTo("Clean Code");
    assertThat(resultat.getAuteurs()).containsExactly("Robert C. Martin");
  }

  @Test
  void bascule_sur_google_books_si_absent_dOpen_library() throws Exception {
    JsonNode openLibraryVide = objectMapper.readTree("{\"docs\": []}");
    JsonNode googleBooksBody =
        objectMapper.readTree(
            "{\"totalItems\": 1, \"items\": [{\"volumeInfo\": {\"title\": \"Effective Java\","
                + " \"authors\": [\"Joshua Bloch\"]}}]}");

    when(restTemplate.getForObject(contains("openlibrary.org"), eq(JsonNode.class), anyString()))
        .thenReturn(openLibraryVide);
    when(restTemplate.getForObject(contains("googleapis.com"), eq(JsonNode.class), anyString()))
        .thenReturn(googleBooksBody);

    LivreDisponibilite resultat = service.rechercherParIsbn("9780134685991");

    assertThat(resultat.isDisponible()).isTrue();
    assertThat(resultat.getSource()).isEqualTo("Google Books");
    assertThat(resultat.getTitre()).isEqualTo("Effective Java");
  }

  @Test
  void retourne_introuvable_si_absent_des_deux_sources() {
    when(restTemplate.getForObject(anyString(), eq(JsonNode.class), anyString())).thenReturn(null);

    LivreDisponibilite resultat = service.rechercherParIsbn("0000000000");

    assertThat(resultat.isDisponible()).isFalse();
    assertThat(resultat.getMessage()).containsIgnoringCase("aucun livre");
  }

  @Test
  void rejette_un_isbn_de_format_invalide() {
    assertThatThrownBy(() -> service.rechercherParIsbn("pas-un-isbn"))
        .isInstanceOf(IsbnInvalideException.class);
  }
}
