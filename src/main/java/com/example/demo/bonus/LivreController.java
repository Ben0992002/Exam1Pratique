package com.example.demo.bonus;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint bonus : vérifie si un livre identifié par son ISBN est disponible.
 *
 * <p>Stratégie de recherche : Open Library en premier, puis Google Books en repli si le livre
 * n'y est pas référencé. Si aucune des deux sources ne le trouve, un message d'erreur explicite
 * est renvoyé (HTTP 404) plutôt qu'une exception technique.
 */
@RestController
@RequestMapping("/bonus/livres")
@AllArgsConstructor
public class LivreController {

  private final LivreLookupService livreLookupService;

  /**
   * GET /bonus/livres/{isbn}
   *
   * @param isbn ISBN-10 ou ISBN-13 du livre recherché (tirets et espaces tolérés)
   * @return 200 avec les détails du livre s'il est trouvé, 404 s'il est introuvable dans les
   *     deux sources, 400 si l'ISBN fourni n'est pas dans un format valide.
   */
  @GetMapping("/{isbn}")
  public ResponseEntity<LivreDisponibilite> verifierDisponibilite(@PathVariable String isbn) {
    LivreDisponibilite resultat = livreLookupService.rechercherParIsbn(isbn);

    if (!resultat.isDisponible()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultat);
    }
    return ResponseEntity.ok(resultat);
  }
}
