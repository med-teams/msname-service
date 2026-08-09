# msname-service

Microservice exemple avec architecture domaine (hexagonale) :

- `domain`        : modele et regles metier pures, sans dependance Spring
- `application`    : orchestration des cas d'usage (use cases)
- `infrastructure` : adapters (REST en entree, JPA en sortie)

A renommer (`msname` -> nom reel du service) avant usage : package Java, artifactId, application.yml.

Depend de `commons-api` (voir ce repo) pour les DTOs de reponse standard et les exceptions communes.

## Lancer en local

    mvn spring-boot:run

## Tester

    curl -X POST localhost:8081/api/products -H "Content-Type: application/json" -d '{"name":"Clavier","price":29.90}'
    curl localhost:8081/api/products
