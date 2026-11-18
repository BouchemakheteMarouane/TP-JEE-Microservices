# ecom-ms-app

Projet Maven multi-modules : **Java 17**, **Spring Boot 3.3.13**,
**Spring Cloud 2023.0.6** (BOM) et **Resilience4j 2.2.0** (version gérée par le BOM).
Cette réalisation suit les exigences fournies ; l'identité exacte avec les vidéos
ne peut pas être certifiée sans leur transcription ou leur code de référence.

## Modules

| Module | Port | Rôle |
| --- | --- | --- |
| discovery-service | 8761 | Registre Eureka et tableau de bord |
| customer-service | 8081 | Clients, JPA/H2 et Spring Data REST |
| inventory-service | 8082 | Produits, JPA/H2 et Spring Data REST |
| gateway-service | 8888 | Passerelle réactive et routage dynamique Eureka |
| billing-service | 8083 | Factures et enrichissement par OpenFeign/Resilience4j |

Chaque module contient son `pom.xml`, sa classe principale, sa configuration et
ses tests. Le POM parent a le packaging `pom` ; les modules sont des applications
JAR autonomes. Aucun module ne dépend directement des classes d'un autre service :
leurs échanges se font par HTTP.

**Attention : bank-service utilise également le port 8081. Il doit être arrêté
avant de lancer customer-service. Aucun fichier de bank-service n'est modifié.**

## Compiler et tester

Ouvrir un terminal dans `D:\Projets\TP-JEE\ecom-ms-app`, avec Maven et un JDK 17 :

```powershell
mvn clean compile
mvn test
mvn package
```

`mvn package` produit un JAR exécutable dans le dossier `target` de chaque module.
Les dépendances sont téléchargées par Maven, sans installation de Java ou d'IDE.
`.mvn/maven.config` conserve le cache Maven dans `.cache/maven` sur D: ; lancer
les commandes depuis ce dossier racine. Le `.gitignore` du dépôt parent exclut
les caches, journaux et dossiers `target`.

Pour employer les outils déjà présents sur cette machine, sans changement
permanent du PATH ou de JAVA_HOME :

```powershell
$env:JAVA_HOME = 'C:\Users\MAPYAH\.jdks\ms-17.0.18'
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\JetBrains\IntelliJ IDEA 2026.3\plugins\maven-plugin\lib\maven3\bin;$env:Path"
mvn clean compile
```

Dans IntelliJ IDEA, ouvrir le POM racine comme projet Maven, importer les cinq
modules et sélectionner le JDK 17 pour le projet et l'exécution Maven.

## Démarrer les services

Exécuter les commandes suivantes dans **cinq terminaux distincts**, tous placés
dans le dossier racine `ecom-ms-app`. Démarrer Eureka en premier et attendre son
démarrage avant les clients :

```powershell
mvn -pl discovery-service spring-boot:run
mvn -pl customer-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl billing-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

Chaque commande reste active dans son terminal. Arrêter avec `Ctrl+C` dans
l'ordre inverse, Eureka en dernier. Les services peuvent aussi être lancés par
leurs méthodes `main` dans IntelliJ, ou par `java -jar` après `mvn package`.

Les clients contactent explicitement `http://localhost:8761/eureka/`.
Leur enregistrement et la propagation des routes ne sont pas instantanés :
attendre qu'ils apparaissent `UP` dans Eureka, puis laisser les caches de
découverte se rafraîchir (en général 30 à 60 secondes).

## URL utiles

- Eureka : `http://localhost:8761/`
- Clients : `http://localhost:8081/customers` et `/customers/1`
- Produits : `http://localhost:8082/products` et `/products/1`
- Facture enrichie : `http://localhost:8083/api/bills/1`
- Via la passerelle :
  - `http://localhost:8888/customer-service/customers`
  - `http://localhost:8888/inventory-service/products`
  - `http://localhost:8888/billing-service/api/bills/1`
- Actuator : `/actuator/health` sur chaque service.
- Console H2 : `/h2-console` sur les ports 8081, 8082 et 8083.

Les préfixes des routes Gateway sont en minuscules et sont supprimés avant
transmission. Le bean explicite `dynamicRoutes` crée le locator ; on n'active
pas simultanément le locator automatique pour éviter les doublons.

Pour H2 : utilisateur `sa`, mot de passe vide, et URL JDBC correspondant au
service : `jdbc:h2:mem:customers-db`, `jdbc:h2:mem:products-db` ou
`jdbc:h2:mem:billing-db`. Les trois bases sont indépendantes et volatiles.

## Données et facturation

- Clients 1 à 3 : Mohamed, Imane et Yassine.
- Produits 1 à 3 : Computer, Printer et Smartphone.
- Trois factures, une par client, avec trois lignes par facture : neuf lignes
  au total, avec prix et quantités aléatoires.

Le démarrage de billing-service ne nécessite pas d'appel aux autres services :
les factures sont initialisées localement. Lors de GET `/api/bills/{id}`, les
informations client et produits sont récupérées par Feign et ajoutées dans les
champs JPA `@Transient`. Le prix de ligne reste celui enregistré dans la facture.
Un `@EntityGraph` charge les lignes avant la sortie du repository, car
`spring.jpa.open-in-view=false`. La référence inverse `ProductItem.bill` est
en écriture seule pour éviter une récursion JSON.

Spring Data REST expose explicitement les identifiants clients/produits pour
que les modèles Feign reçoivent aussi leurs IDs. Les factures inconnues renvoient
HTTP 404.

## Tolérance aux pannes

Les méthodes Feign portent `@CircuitBreaker` avec les noms `customerService`
et `inventoryService`. `resilience4j-spring-boot3` et le starter AOP activent
réellement ces annotations sur les proxies ; le starter Spring Cloud seul
ne suffit pas à installer leurs aspects.

Les fallbacks conservent l'identifiant demandé et renvoient `Default Customer`
ou `Default Product`. Le produit de secours a un prix et une quantité à zéro ;
cela ne modifie pas les valeurs de la ligne de facture. Les délais Feign de
connexion et de lecture sont limités à deux secondes chacun. Ces fallbacks
signalent des données de secours, pas une synchronisation des bases.

Les tests désactivent les appels au registre Eureka extérieur. Ils vérifient
les données et les API Data REST, le tableau de bord Eureka sur port aléatoire,
les routes dynamiques et la facturation. Les tests Feign utilisent un serveur
HTTP local de test : réponses nominales et réponses 503 pour déclencher les
vrais aspects Resilience4j et vérifier leurs compteurs.

## Limites du TP

Projet pédagogique sans authentification ni stockage durable. Les consoles H2
ne doivent pas être exposées publiquement. Les versions 3.3.x / 2023.0.x sont
conservées conformément à la demande, sans désactiver le vérificateur de
compatibilité Spring Cloud. Aucune fonctionnalité GraphQL n'est ajoutée.

## Vérifications réalisées

- `mvn clean compile` depuis la racine : **BUILD SUCCESS**, parent et cinq
  modules reconstruits avec `javac --release 17`.
- `mvn test` : **BUILD SUCCESS**, dix tests exécutés, aucun échec ni erreur :
  Eureka (1), clients (2), produits (2), passerelle (1), facturation (4).
- Les tests de facturation vérifient les trois factures et neuf lignes, les
  échanges HTTP Feign, les deux fallbacks et les compteurs Resilience4j.
- Les services démarrés par les tests sont arrêtés à leur fin. Le cluster complet
  n'a pas été lancé simultanément sur ses cinq ports dans cette validation.
- `bank-service` est inchangé et aucun commit n'a été créé.

Les avertissements de cache LoadBalancer sans Caffeine et d'absence de pairs
pour Eureka autonome sont non bloquants dans ce TP. Eureka peut aussi signaler
au niveau INFO l'absence d'un fournisseur Bean Validation : aucune contrainte de
validation de ce type n'est utilisée ici. L'avertissement JVM de partage de
classes pendant les tests provient de l'instrumentation Mockito. Aucun niveau
de journalisation n'a été abaissé pour masquer ces messages.
