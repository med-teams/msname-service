# catalog-service

Catalogue produits du SaaS : categories, produits et cycle de vie `DRAFT -> ACTIVE -> ARCHIVED`.
Spring Boot 3.3, Java 17, architecture hexagonale, depend de `commons-api` (reponses, erreurs, `BaseEntity`).

## Architecture

| Couche | Package | Role |
|---|---|---|
| Domaine | `domain.model`, `domain.repository` | Regles metier pures (`Product`, `Sku`, `ProductStatus`, `Category`) et ports de persistance. Aucune dependance Spring ni JPA. |
| Application | `application` | Cas d'usage transactionnels (`ProductService`, `CategoryService`) et DTOs valides (Bean Validation). |
| Infrastructure | `infrastructure.persistence` | Adapters JPA (entites heritant de `BaseEntity`, Specifications pour la recherche). |
| | `infrastructure.web` | Controleurs REST `/api/v1`, `GlobalExceptionHandler`. |

Patterns utilises : ports & adapters, value object (`Sku`), machine a etats (`ProductStatus`),
factory methods (`Product.create` / `Product.restore`), Specification (filtres de recherche),
verrou optimiste (`@Version` de `BaseEntity`).

## API

| Methode | Route | Description |
|---|---|---|
| POST | `/api/v1/categories` | Cree une categorie (201) |
| GET | `/api/v1/categories`, `/api/v1/categories/{id}` | Liste, detail |
| POST | `/api/v1/products` | Cree un produit en brouillon (201) |
| GET | `/api/v1/products?status=&categoryId=&page=&size=` | Recherche paginee (size <= 100) |
| GET | `/api/v1/products/{id}` | Detail |
| PUT | `/api/v1/products/{id}` | Modifie un produit (422 s'il est archive) |
| POST | `/api/v1/products/{id}/activate`, `/archive` | Change le statut (422 si transition interdite) |

Erreurs : format `ErrorResponse` de commons-api. 400 `VALIDATION_ERROR` (avec le detail par champ) ou
`MALFORMED_REQUEST`, 404 `NOT_FOUND`, 409 `CONFLICT` (SKU deja utilise, modification concurrente),
422 `BUSINESS_RULE_VIOLATION`, 500 `INTERNAL_ERROR` (sans detail technique, journalisee).

Documentation interactive : http://localhost:8081/swagger-ui.html. Sante : `/actuator/health`.

## Lancer en local

    mvn spring-boot:run

    curl -X POST localhost:8081/api/v1/categories -H "Content-Type: application/json" -d '{"name":"Peripheriques"}'
    curl -X POST localhost:8081/api/v1/products -H "Content-Type: application/json" \
         -d '{"sku":"kb-001","name":"Clavier","price":29.90,"categoryId":1}'
    curl -X POST localhost:8081/api/v1/products/1/activate
    curl "localhost:8081/api/v1/products?status=ACTIVE"

Base H2 en memoire ; le schema est cree par Flyway (`src/main/resources/db/migration`) et Hibernate
ne fait que le valider (`ddl-auto: validate`).

## Tests

    mvn verify

Domaine (unitaires), cas d'usage (Mockito), couche web (`@WebMvcTest`), persistance (`@DataJpaTest` sur
le schema Flyway) et parcours complet (`@SpringBootTest`).
