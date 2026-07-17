# poja-starter-template

## 🎁 Bonus — Vérification de disponibilité d'un livre par ISBN

Endpoint isolé dans le package [`com.example.demo.bonus`](src/main/java/com/example/demo/bonus),
indépendant du reste du projet d'examen.

**`GET /bonus/livres/{isbn}`**

Stratégie de recherche :
1. Interroge [Open Library](https://openlibrary.org/) (Option A).
2. Si le livre n'y est pas référencé, bascule automatiquement sur [Google Books](https://developers.google.com/books).
3. Si le livre est introuvable dans les deux sources, retourne une réponse `404`
   avec un message d'erreur explicite (pas d'exception technique renvoyée au client).
4. Si l'ISBN fourni n'a pas un format valide, retourne `400`.

Fichiers :
- [`LivreController.java`](src/main/java/com/example/demo/bonus/LivreController.java) — endpoint REST
- [`LivreLookupService.java`](src/main/java/com/example/demo/bonus/LivreLookupService.java) — logique de recherche (Open Library puis Google Books)
- [`LivreDisponibilite.java`](src/main/java/com/example/demo/bonus/LivreDisponibilite.java) — DTO de réponse
- [`IsbnInvalideException.java`](src/main/java/com/example/demo/bonus/IsbnInvalideException.java) / [`BonusExceptionHandler.java`](src/main/java/com/example/demo/bonus/BonusExceptionHandler.java) — validation et gestion d'erreur
- Documentation : [`doc/api.yml`](doc/api.yml)
- Tests : [`src/test/java/com/example/demo/bonus`](src/test/java/com/example/demo/bonus)

Exemple :
```
GET /bonus/livres/978-0-13-235088-4
→ 200 { "isbn": "9780132350884", "disponible": true, "source": "Open Library", "titre": "Clean Code", "auteurs": ["Robert C. Martin"], "message": "Livre trouvé sur Open Library." }
```
