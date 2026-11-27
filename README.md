# Le-Repo-Du-Royaume

## Lancer le projet avec Docker (Vierge vs Démo)

Les images Docker utilisent les livrables compilés (`royaume_back/target/*.jar` et `royaume_front/dist/royaume_front/browser/`), comme vu en cours.

### 1. Préparer les builds
```bash
cd royaume_back && ./mvnw clean package && cd ..
cd royaume_front && npm i && npx ng build && cd ..
```

### 2. Lancer la version Vierge (vraie partie à zéro)
Contient uniquement les données de référentiel (5 peuples + 9 paliers de niveau), sans aucune partie ni joueur :
```bash
docker compose -f docker-compose.yml up -d --build
```
- **Front Angular** : http://localhost:82
- **API Spring Boot** : http://localhost:8091
- **phpMyAdmin** : http://localhost:8082 (`serveur: db`, `user: root`, `mdp: root`)

### 3. Lancer la version Démo (données pré-remplies)
Active le profil Spring `demo` (`DataInitializer`) avec 5 royaumes, bâtiments, marché, attaques, `"Partie Démo - En cours"` et `"Salon Démo - Ouvert"` :
```bash
docker compose -f docker-compose.demo.yml up -d --build
```
- **Front Angular** : http://localhost:83
- **API Spring Boot** : http://localhost:8092
- **phpMyAdmin** : http://localhost:8083 (`serveur: db`, `user: root`, `mdp: root`)
- **Comptes de démo** (`motdepasse123`) : `Kat` (admin, Royaume des Canards), `Kit` (Royaume des Teuteus), `Demo` (sans royaume, prêt à rejoindre le salon)

Les deux versions utilisent des réseaux, volumes et ports distincts : elles peuvent tourner en même temps.

---

## Développement local : choisir entre BDD locale (WAMP/MAMP) et BDD distante

Chaque développeur conserve son fichier ignoré par Git `royaume_back/src/main/resources/application-local.properties` (créé à partir de `application-local.properties.example`).

- **BDD locale (WAMP / MAMP / MySQL local)** : configurez votre port et mot de passe dans `application-local.properties`.
- **BDD distante du groupe (`20.84.69.4:3326`)** :
  - Option 1 : décommentez l'**Option E** dans votre `application-local.properties` (en renseignant le mot de passe en local uniquement).
  - Option 2 : lancez Spring Boot avec le profil `remote` et la variable d'environnement `REMOTE_DB_PASSWORD` :
    ```bash
    REMOTE_DB_PASSWORD=<mot_de_passe> ./mvnw spring-boot:run -Dspring-boot.run.profiles=remote
    ```

Côté Angular en dev (`npm start`), `proxy.conf.json` redirige automatiquement `/api/*` vers `http://localhost:8080`.

---

## Jouer en multijoueur (BDD distante, mode Host / Invité)

Sur la branche dédiée `multi-host-guest`, chaque joueur peut lancer son propre Spring Boot + Angular en local tout en partageant la base MySQL distante (un seul joueur en mode **Host** avec moteur de jeu actif, les autres en mode **Invité** sans scheduler).

Consultez le guide complet [MULTIJOUEUR.md](./MULTIJOUEUR.md) (procédure Docker Windows/macOS/Linux, Plan B sans Docker et Plan C SSH).

---

## Pipeline CI/CD (Jenkins & SonarQube)

- **Jenkins** : http://20.84.69.4:8080/job/royaume-g2/
- **SonarQube** : http://20.84.69.4:9000 (clé projet : `royaume-back`)
- Le `Jenkinsfile` à la racine exécute :
  1. Checkout de la branche `dev`
  2. `mvn clean verify` + analyse SonarQube dans `maven:3.10.0-eclipse-temurin-21`
  3. `npm i` + `ng build` dans `node:lts`
  4. Déploiement des deux stacks `docker-compose.yml` (Vierge) et `docker-compose.demo.yml` (Démo)

---

### Ce qu'il manque

les capacités des peuples
Les unités spéciales

Ajuster les couts des unités
