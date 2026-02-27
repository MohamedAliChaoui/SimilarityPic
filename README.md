# Projet L1C - Application de Gestion d'Images

## Prérequis

- Java 17 ou supérieur
- Node.js 16 ou supérieur
- PostgreSQL 14 ou  ou supérieur
- Maven
- npm
- Docker (optionnel)

### Configuration de la Base de Données
1. Créer une base de données PostgreSQL :
```sql
CREATE DATABASE similaritypic;
```

2. Ajouter l'extension vector à la BDD :
```sql
\c similaritypic
CREATE EXTENSION IF NOT EXISTS vector;
```


## Configuration du Backend

1. Copier le fichier `application.properties.example` en `application.properties` :
```bash
cd backend/src/main/resources
cp application.properties.example application.properties
```

2. Modifier les paramètres de connexion via des variables d'environnement, ou directement dans `application.properties` (par défaut `similaritypic` et `postgres/postgres`).

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/similaritypic
spring.datasource.username=postgres
spring.datasource.password=postgres
cors.allowed-origins=http://localhost:5173
```

## Démarrage du Projet

### Backend

1. Se placer dans le dossier backend :
```bash
cd backend
```

2. Installer les dépendances :
```bash
mvn clean install
```

3. Démarrer l'application :
```bash
mvn spring-boot:run
```

Le serveur backend sera accessible sur : `http://localhost:8001/`

### Frontend

1. Se placer dans le dossier frontend :
```bash
cd frontend
```

2. Installer les dépendances :
```bash
npm install
```

3. Démarrer l'application en mode développement :
```bash
npm run dev
```

Le frontend sera accessible sur : `http://localhost:5173/`

## Structure du Projet

- `backend/` : Application Spring Boot
  - `src/main/java/pdl/backend/` : Code source Java
  - `src/main/resources/` : Fichiers de configuration
  - `src/main/resources/db/migration/` : Scripts de migration Flyway

- `frontend/` : Application Vue.js
  - `src/` : Code source TypeScript/Vue
  - `public/` : Fichiers statiques

## Fonctionnalités Principales

- Authentification des utilisateurs
- Upload et gestion d'images
- Favoris et sauvegarde d'images
- Analyse d'images avec descripteurs
- Feed d'images publiques
- Galerie personnelle

## Développement

### Branches Git

- `main` : Branche principale
- `develop` : Branche de développement
- `feature/*` : Branches de fonctionnalités

### Conventions de Code

- Backend : Suivre les conventions Java
- Frontend : Suivre les conventions Vue.js/TypeScript
- Messages de commit : Format conventionnel (feat:, fix:, etc.)

## Support

Pour toute question ou problème, contacter l'équipe de développement.

# Application Client-Serveur - Recherche d'Images par Similarité

## ✨ Description du projet

Une application **client-serveur** permettant la **recherche d'images par similarité**.
Elle utilise **Spring Boot** pour le backend et **Vue.js** pour le frontend.
Les images sont indexées dans une base **PostgreSQL avec pgvector** pour une recherche avancée.

**Projet réalisé par le groupe "L1C"** :

- **Mohamed Ali Chaoui**
- **Mouhamed Ndiaye**
- **Yassine Zouitni**

Travail collaboratif via **Live Share** et **CI/CD avec GitLab**.

---

## 🔹 Prérequis

### ✅ Systèmes d'exploitation testés

✔ **Windows 11** (testé sur PC perso par **Yassine** et **Mohamed Ali**)  
### ✅ Navigateurs compatibles

✔ **Google Chrome**  
✔ **Brave**  
✔ **Safari**

---

## 🔹 Installation et Exécution

### 📂 Backend (Spring Boot + PostgreSQL)

#### 1️⃣ Installer les dépendances

Assurez-vous que PostgreSQL est installé et lancez :

```bash
cd backend
mvn clean install
```

#### 2️⃣ Configurer la base de données

Créez une base PostgreSQL et mettez à jour `application.properties` :

```properties
spring.datasource.url = jdbc:postgresql://localhost:5432/${DATABASE_NAME}
spring.datasource.username = ${DATABASE_NAME}
spring.datasource.password = ${DATABASE_PASSWORD}
```

#### 3️⃣ Créer un fichier `.env` avec les coordonnées de connexion BDD

#### 4️⃣ Démarrer le serveur

```bash
mvn spring-boot:run
```

💽 **Le serveur démarre sur `http://localhost:8001/`**.

---

### 🎨 Frontend (Vue.js + TypeScript)

#### 1️⃣ Installer les dépendances

```bash
cd frontend
npm install
```

#### 2️⃣ Lancer le serveur Vue.js

```bash
npm run dev
```

💽 **Le site sera accessible sur `http://localhost:5173/`**.

#### 3️⃣ Construire l'application pour la production

```bash
npm run build
```

---

## 🔹 Gestion Git et Collaboration

### 📥 Cloner le projet

```bash
git clone <votre_url_github>
cd similaritypic
```

---

## 🔹 Exemples de requêtes API

### ✅ Lister les images disponibles

```bash
curl -X GET http://localhost:8001/images
```

### ✅ Ajouter une image

```bash
curl -X POST -F "file=@image.jpg" http://localhost:8001/images
```

### ✅ Supprimer une image

```bash
curl -X DELETE http://localhost:8001/images/1
```

### ✅ Rechercher des images similaires

```bash
curl -X GET "http://localhost:8001/images/1/similar?number=5&descriptor=rgb"
```

---

## 🔹 Tests et Intégration Continue

✔ **Tests unitaires avec JUnit** (`mvn test`)  
✔ **CI/CD via GitLab**
