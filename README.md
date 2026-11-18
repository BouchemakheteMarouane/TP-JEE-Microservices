# Travaux pratiques Java JEE

Dépôt regroupant plusieurs TP, chacun dans son propre dossier.

## Structure

```text
TP-JEE/
├── .gitignore
├── README.md
└── bank-service/
    ├── pom.xml
    ├── src/main/
    ├── src/test/
    ├── README.md
    └── AUDIT-REST.md
```

## TP 1 — bank-service (Part 2)

Microservice REST de gestion de comptes bancaires, développé avec Java 17,
Spring Boot 2.7.18 et Maven :

- Spring Data JPA et base H2 en mémoire pour la persistance ;
- API REST personnalisée avec opérations CRUD ;
- Spring Data REST pour l'exposition du repository, la recherche et les projections ;
- DTO et mapper utilisant `BeanUtils.copyProperties` ;
- documentation Swagger UI / OpenAPI ;
- tests unitaires et d'intégration.

Le projet ne contient ni GraphQL ni entité Customer. Il démarre sur le port 8081.
Les instructions d'exécution et les résultats d'audit figurent dans
[`bank-service/README.md`](bank-service/README.md) et
[`bank-service/AUDIT-REST.md`](bank-service/AUDIT-REST.md).

Les prochains TP seront ajoutés dans d'autres sous-dossiers de ce dépôt unique.
Les caches, fichiers IntelliJ, journaux et résultats de compilation sont exclus de Git.
