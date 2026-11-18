# bank-service

TP de microservice bancaire : Java 17, Spring Boot 2.7.18 et Maven.
Springdoc OpenAPI est fixé à la version 1.6.11.

## Exécution

Avec Maven et un `JAVA_HOME` valide pointant vers un JDK 17 :

```powershell
mvn clean compile
mvn test
mvn spring-boot:run
```

Dans IntelliJ IDEA : ouvrir `pom.xml` comme projet Maven, sélectionner un
JDK 17 dans **File > Project Structure > Project SDK**, puis sélectionner ce
même JDK pour l'import et l'exécution Maven dans **Settings > Build, Execution,
Deployment > Build Tools > Maven**. Le Maven intégré à IntelliJ convient.
Lancer ensuite la méthode `main` de `BankServiceApplication`.
L'import Maven peut télécharger les dépendances déclarées dans `pom.xml`.

Le disque C: étant plein lors de la vérification initiale, `.mvn/maven.config`
place le cache de dépendances dans `.cache/maven`, à l'intérieur du projet sur
D:, sans modifier le cache Maven global. Exécuter les commandes depuis le
dossier contenant ce `pom.xml`. Le `JAVA_HOME` utilisateur actuellement défini
sur la machine pointe vers un ancien dossier inexistant ; aucune variable
permanente n'a été modifiée. Dans IntelliJ, sélectionner explicitement le JDK
17 existant plutôt que ce `JAVA_HOME` invalide.

Pour utiliser les outils déjà présents depuis PowerShell, sans modification
permanente des variables d'environnement, ouvrir un terminal dans ce dossier :

```powershell
$env:JAVA_HOME = 'C:\Users\MAPYAH\.jdks\ms-17.0.18'
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\JetBrains\IntelliJ IDEA 2026.3\plugins\maven-plugin\lib\maven3\bin;$env:Path"
New-Item -ItemType Directory -Path .cache\tmp -Force | Out-Null
$env:TEMP = (Resolve-Path .cache\tmp).Path
$env:TMP = $env:TEMP
mvn clean compile
mvn test
mvn spring-boot:run
```

## Points d'accès

- API personnalisée : `http://localhost:8081/api/bankAccounts`
- API Spring Data REST : `http://localhost:8081/bankAccounts`
- Recherche par type : `http://localhost:8081/bankAccounts/search/byType?t=CURRENT_ACCOUNT`
- Projection : `http://localhost:8081/bankAccounts?projection=p1`
- Swagger UI : `http://localhost:8081/swagger-ui.html`
- Description OpenAPI : `http://localhost:8081/v3/api-docs`
- Console H2 : `http://localhost:8081/h2-console`
  - URL JDBC : `jdbc:h2:mem:account-db`
  - Utilisateur : `sa`
  - Mot de passe : vide

L'API personnalisée expose GET (liste et détail), POST, PUT et DELETE.
Exemple de corps JSON pour POST :

```json
{
  "balance": 1500.0,
  "currency": "MAD",
  "type": "CURRENT_ACCOUNT"
}
```

Pour PUT `/api/bankAccounts/{id}`, seuls les champs non nuls sont modifiés.
Un corps `{"balance": 2000.0}` conserve donc la devise, le type, l'identifiant
et la date de création. Un identifiant introuvable provoque une
`RuntimeException`, sans traduction en HTTP 404, conformément au TP.

## Architecture

`web` reçoit les requêtes HTTP. `service` porte les opérations métier et les
transactions. `repositories` utilise Spring Data JPA pour accéder à H2 et
Spring Data REST pour exposer automatiquement le repository. `entities`
contient le modèle persistant et la projection `p1`. `dto` décrit les données
d'entrée et de sortie ; `mappers` copie les propriétés de l'entité vers le DTO
avec `BeanUtils`. `enums` définit les types de comptes.

Lombok génère les accesseurs, constructeurs et builders. Springdoc fournit
la documentation OpenAPI et Swagger UI. `BankServiceApplication` démarre
Spring Boot et insère dix comptes aléatoires avec des types alternés.

H2 est en mémoire : les données disparaissent à l'arrêt et dix nouveaux comptes
sont créés au redémarrage. Ce projet pédagogique utilise une ancienne branche
Spring Boot, ne comporte pas d'authentification et active la console H2 : il
n'est pas destiné à une exposition publique en production.

## Vérifications réalisées

- `mvn clean compile` : `BUILD SUCCESS` avec le JDK 17.0.18 existant.
- `mvn package` : JAR exécutable généré et 9 tests réussis, aucun échec.
- JAR démarré sur le port 8081 : dix comptes présents ; GET, POST, PUT partiel
  et DELETE vérifiés par HTTP ; recherche par type et projection vérifiées.
- OpenAPI, Swagger UI et console H2 : réponses HTTP 200.
- Le processus de vérification a été arrêté après les contrôles.
