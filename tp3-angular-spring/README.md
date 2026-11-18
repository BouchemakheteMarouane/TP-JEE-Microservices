# TP 3 — Angular 19 et Spring Boot

```text
tp3-angular-spring/
├── product-backend/   # Java 17, Spring Boot 3.3.13, JPA/H2, Springdoc 2.6.0
└── product-frontend/  # Angular 19 standalone, routage, HttpClient, Bootstrap
```

## Lancement

### Terminal 1 : backend

Avec un JDK 17 et Maven, depuis `product-backend` :

```powershell
Set-Location D:\Projets\TP-JEE\tp3-angular-spring\product-backend
mvn clean compile
mvn test
mvn spring-boot:run
```

Le cache Maven est local au backend (`.cache/maven`) et exclu de Git.
Dans IntelliJ, ouvrir `product-backend/pom.xml`, choisir le JDK 17 pour le
projet et Maven, puis lancer `ProductBackendApplication`.

Si Maven est absent du PATH, les outils déjà présents peuvent être sélectionnés
pour ce terminal uniquement, avant les commandes précédentes :

```powershell
$env:JAVA_HOME = 'C:\Users\MAPYAH\.jdks\ms-17.0.18'
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\JetBrains\IntelliJ IDEA 2026.3\plugins\maven-plugin\lib\maven3\bin;$env:Path"
```

### Terminal 2 : frontend

```powershell
Set-Location D:\Projets\TP-JEE\tp3-angular-spring\product-frontend
npm ci
npm start -- --port 4200
```

`npm start` utilise le CLI Angular 19 local, même si le CLI global est d'une
autre version. Le projet a été généré sans SSR ni dépôt Git imbriqué avec
`npx -y @angular/cli@19`. Bootstrap et Bootstrap Icons sont importés dans
`src/styles.css`. Pour compiler la version de production : `npm run build`.

Pour les tests Angular dans un navigateur installé : `npm test -- --watch=false`.
Avec Edge déjà présent sur cette machine, les tests headless peuvent utiliser
le lanceur Chromium sans installer Chrome :

```powershell
$env:CHROME_BIN = 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
npm test -- --watch=false --browsers=ChromeHeadless
```

## URL de test

| URL | Rôle |
| --- | --- |
| http://localhost:4200/ | Redirection vers l'accueil |
| http://localhost:4200/home | Carte de bienvenue |
| http://localhost:4200/products | Tableau et suppression avec confirmation |
| http://localhost:8083/products | GET : liste JSON des produits |
| http://localhost:8083/products/1 | GET : détail ; DELETE : suppression |
| http://localhost:8083/swagger-ui.html | Swagger UI |
| http://localhost:8083/v3/api-docs | Document OpenAPI |
| http://localhost:8083/h2-console | Console H2 |

H2 : URL JDBC `jdbc:h2:mem:products-db`, utilisateur `sa`, mot de passe vide.
La base est en mémoire : chaque redémarrage restaure les trois produits du TP.

| Produit | Prix | Quantité | Sélectionné |
| --- | --- | --- | --- |
| Computer | 1300 | 10 | Oui |
| Printer | 450 | 20 | Non |
| Smartphone | 900 | 15 | Oui |

Le frontend appelle directement l'API sur le port 8083 ; `@CrossOrigin("*")`
autorise ces requêtes. Un produit absent renvoie HTTP 404 ; une suppression
réussie renvoie HTTP 200 sans corps, puis Angular recharge la liste.

`ProductService` porte les appels HTTP typés. `ProductsComponent` gère le
chargement, les erreurs, la confirmation et la suppression. Les templates
utilisent `@if` et `@for (...; track p.id)`. La navigation utilise le routeur
Angular ; aucun JavaScript Bootstrap n'est nécessaire pour cette barre simple.

## Précautions

- Arrêter `ecom-ms-app/billing-service` avant ce backend : ils utilisent tous
  deux le port 8083. Le port 4200 doit également être libre.
- Ce TP n'ajoute ni authentification ni serveur de production. La console H2
  et le CORS permissif sont destinés au développement local.
- `node_modules`, `.angular`, `dist`, les caches et `target` sont exclus par
  le `.gitignore` du dépôt parent. Conserver `package-lock.json` dans Git.
- Aucun autre TP n'est modifié et aucun commit n'est créé automatiquement.

## Vérification

- Environnement : Node.js 22.23.5, npm 10.9.9 ; CLI global 21.2.8 conservé.
  Le projet utilise Angular 19.2.25 et le CLI local 19.2.27.
- Backend : `mvn clean compile` réussi et **7 tests d'intégration réussis**.
- Frontend : `npm run build` réussi et **11 tests réussis** sous Edge headless.
- Build production : bundle initial de 585,57 kB, au-dessus du seuil
  d'avertissement de 500 kB ; quatre sélecteurs Bootstrap sont signalés par
  l'optimiseur de CSS critique. Le build termine sans erreur ; les seuils et
  diagnostics n'ont pas été désactivés pour masquer ces avertissements.
- Aucun dépôt Git imbriqué ; les caches et artefacts sont bien ignorés.

**Sécurité :** l'audit npm réalisé lors de la création relève **43 vulnérabilités**
(2 faibles, 25 modérées, 14 élevées, 2 critiques), incluant les outils de
développement. `npm audit --omit=dev` en relève 7 dans les dépendances de
production (3 modérées, 4 élevées, aucune critique). Ces résultats ne constituent
pas une preuve d'exploitation de cette application, mais interdisent de la
présenter comme prête pour la production. Ne pas l'exposer publiquement.

Angular 19 est maintenu pour rester conforme au cours. Aucun `npm audit fix
--force` ni changement de version majeure n'a été appliqué : les correctifs
proposés par npm nécessitent notamment une migration hors d'Angular 19.
Les rapports complets sont dans `.cache/npm-audit.json` et
`.cache/npm-audit-production.json` (non suivis par Git).
