package com.example.demo.bonus;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LivreController.class)
class LivreControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LivreLookupService livreLookupService;

  @Test
  void retourne_200_avec_les_details_si_le_livre_est_disponible() throws Exception {
    when(livreLookupService.rechercherParIsbn(eq("9780132350884")))
        .thenReturn(
            LivreDisponibilite.disponible(
                "9780132350884", "Open Library", "Clean Code", List.of("Robert C. Martin")));

    mockMvc
        .perform(get("/bonus/livres/9780132350884"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.disponible").value(true))
        .andExpect(jsonPath("$.source").value("Open Library"))
        .andExpect(jsonPath("$.titre").value("Clean Code"));
  }

  @Test
  void retourne_404_si_le_livre_est_introuvable() throws Exception {
    when(livreLookupService.rechercherParIsbn(eq("0000000000")))
        .thenReturn(LivreDisponibilite.introuvable("0000000000"));

    mockMvc
        .perform(get("/bonus/livres/0000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.disponible").value(false));
  }

  @Test
  void retourne_400_si_lisbn_est_invalide() throws Exception {
    when(livreLookupService.rechercherParIsbn(eq("pas-un-isbn")))
        .thenThrow(new IsbnInvalideException("pas-un-isbn"));

    mockMvc.perform(get("/bonus/livres/pas-un-isbn")).andExpect(status().isBadRequest());
  }
}
