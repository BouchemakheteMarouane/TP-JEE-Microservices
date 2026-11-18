# Audit du microservice REST bank-service

Date : 18 novembre 2026.

## Conclusion et limite de certification

Les critères explicitement demandés pour le connecteur REST sont vérifiés après
correction. La transcription ou le code final officiel de la vidéo n'étant pas
disponible, cet audit ne certifie pas une identité à 100 % avec son état final.
Aucune fonctionnalité GraphQL ni entité Customer n'a été ajoutée.

## Structure et dépendances

- Package racine : `org.sid.bankservice`.
- Packages : `entities`, `enums`, `repositories`, `dto`, `mappers`, `service`, `web`.
- Java 17, Spring Boot 2.7.18, Maven intégré à IntelliJ déjà installé.
- Dépendances : Spring Web, Spring Data JPA, Spring Data REST, H2, Lombok,
  Springdoc OpenAPI UI 1.6.11 et Spring Boot Starter Test.
- JPA utilise `javax.persistence`, cohérent avec Spring Boot 2.7.x.
- Entité BankAccount, enum AccountType, projection p1 et recherche byType présents.
- Aucun fichier source ou dépendance GraphQL/Customer identifié.

## Vérification des critères et correction

| Point | Résultat |
| --- | --- |
| AccountMapper annoté @Component | Conforme |
| BankAccount vers BankAccountResponseDTO avec BeanUtils.copyProperties | Déjà conforme |
| BankAccountRequestDTO vers BankAccount avec BeanUtils.copyProperties | Méthode fromBankAccountRequestDTO ajoutée |
| AccountServiceImpl annoté @Service et @Transactional | Déjà conforme |
| Injection par constructeur | Conforme dans le service et le contrôleur |
| addAccount utilise AccountMapper pour les deux conversions | Corrigé : suppression de la construction manuelle par builder |
| UUID et createdAt à la création | Générés par le service après conversion du DTO |
| POST/PUT/DELETE délégués à AccountService | Déjà conforme |
| Contrôleur personnalisé utilisant les DTO en entrée/sortie | Déjà conforme |
| spring.jpa.open-in-view=false | Déjà présent à l'ouverture de cet audit, conservé sans modification |

La mise à jour partielle conserve ses contrôles de champs non nuls : remplacer
ces contrôles par une copie inconditionnelle écraserait les valeurs existantes
avec les champs nuls du DTO. Les conversions de création et de réponse passent
bien par AccountMapper.

Les GET du contrôleur lisent le repository et retournent des DTO via le mapper.
L'API automatique Spring Data REST sous `/bankAccounts` reste distincte de
l'API personnalisée sous `/api/bankAccounts` ; elle expose volontairement le
repository et sa projection, conformément aux spécifications du projet.

## Modifications effectuées

1. `src/main/java/org/sid/bankservice/mappers/AccountMapper.java` : ajout de la
   conversion DTO de requête vers entité par BeanUtils.copyProperties.
2. `src/main/java/org/sid/bankservice/service/AccountServiceImpl.java` : utilisation
   de cette conversion dans addAccount, puis initialisation de l'UUID et de la date.
3. `src/test/java/org/sid/bankservice/mappers/AccountMapperTests.java` : deux tests
   unitaires sur les conversions et la responsabilité du service pour l'identité.
4. Le présent rapport.

Aucun changement du pom.xml, du contrôleur ou de application.properties.
Aucune installation et aucune modification permanente des variables d'environnement.

## Compilation et tests

Maven a été exécuté hors ligne avec le JDK Microsoft OpenJDK 17.0.18 existant.

- `mvn clean test-compile` : BUILD SUCCESS ; reconstruction de 11 sources
  applicatives et compilation de 2 classes de tests.
- `mvn test` : BUILD SUCCESS ; 9 tests d'intégration et 2 tests unitaires du mapper.
- Total : 11 tests, 0 échec, 0 erreur, 0 test ignoré.
- Aucun WARN ou ERROR relevé dans la sortie de cette exécution des tests.
- Rapports : `target/surefire-reports/`.

## Validation HTTP réelle

Application démarrée depuis les classes fraîchement compilées, sous Java
17.0.18. Tomcat a démarré sur le port 8081 en environ 6,2 secondes.
La base utilisée est exclusivement `jdbc:h2:mem:account-db`.

Base des URL : `http://localhost:8081`.
Compte temporaire : `cccfa8dc-13cb-4a31-bfb0-5ddc3c5ffc7e`.

| Méthode | Chemin | HTTP | Vérification |
| --- | --- | --- | --- |
| GET | /api/bankAccounts | 200 | 10 comptes initiaux, JSON |
| POST | /api/bankAccounts | 200 | Création : balance 1500.0, MAD, CURRENT_ACCOUNT ; UUID et date présents |
| GET | /api/bankAccounts | 200 | 11 comptes, dont le compte créé |
| GET | /api/bankAccounts/cccfa8dc-13cb-4a31-bfb0-5ddc3c5ffc7e | 200 | Données du compte créé correctement relues |
| DELETE | /api/bankAccounts/cccfa8dc-13cb-4a31-bfb0-5ddc3c5ffc7e | 200 | Nettoyage du compte temporaire |
| GET | /api/bankAccounts | 200 | Retour aux 10 comptes de démonstration, strictement inchangés |

Les réponses GET/POST sont en `application/json` ; DELETE retourne un corps vide.
Aucune ligne WARN ou ERROR dans les journaux de cette passe finale, du démarrage
à l'arrêt, sans baisse du niveau de journalisation ni masquage de messages.

## Arrêt propre et preuves

L'application a été démarrée et arrêtée par les objectifs Maven
`spring-boot:start` / `spring-boot:stop`. Le contrôle administratif JMX temporaire
était limité à l'interface locale, sans ajout dans la configuration du projet.
Il ne constitue pas une fonctionnalité HTTP supplémentaire.

Les journaux confirment la demande d'arrêt, l'arrêt de Tomcat, la fermeture de
l'EntityManagerFactory et `HikariPool-1 - Shutdown completed`. Les ports 8081
et 9001 sont libres après validation. Aucun arrêt forcé n'a été utilisé.

Preuves de la passe finale, dans `target/` :

- `rest-audit-20261118-191811-948.json` : requêtes et réponses complètes.
- `rest-audit-20261118-191811-948-start.log` : démarrage, requêtes et fermeture.
- `rest-audit-20261118-191811-948-stop.log` : arrêt Maven réussi.
- Les fichiers `-start.err.log` et `-stop.err.log` correspondants sont vides.

Ces preuves sont des artefacts de validation ; un prochain `mvn clean` les supprimera.

## Comportement connu hors correction demandée

Le code conserve les RuntimeException pour les comptes introuvables, donc le
comportement HTTP 500 précédemment constaté. Aucun changement vers HTTP 404 n'a
été effectué. L'absence d'avertissement attestée ici concerne le scénario nominal
testé ; elle ne garantit pas que des requêtes volontairement invalides ne produiront
jamais d'erreurs ou d'avertissements dans les journaux.
