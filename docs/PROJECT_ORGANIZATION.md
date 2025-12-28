# 📂 Organisation du Projet - Planner Intelligent

## Structure du Projet

```
planner_app_multi/
├── assets/                     # Ressources statiques centralisées
│   ├── css/
│   │   └── style.css          # Styles globaux de l'application
│   └── js/
│       ├── shared.js          # Fonctions partagées et API client
│       └── add-test-data.js   # Script de génération de données
│
├── pages/                      # Pages HTML de l'application
│   ├── activities.html        # Gestion des activités
│   ├── constraints.html       # Gestion des contraintes
│   ├── planning.html          # Vue planning hebdomadaire
│   ├── optimisation.html      # Optimisation automatique
│   ├── stats.html             # Statistiques et fatigue
│   ├── login.html             # Authentification
│   └── add-test-data.html     # Utilitaire de seeding
│
├── docs/                       # Documentation du projet
│   ├── FEATURES.md            # Fonctionnalités implémentées
│   ├── ARCHITECTURE.md        # Architecture technique
│   └── PROJECT_ORGANIZATION.md # Ce fichier
│
├── database/                   # Scripts de base de données
│   ├── init-mysql.sql         # Schéma initial MySQL
│   └── README-MYSQL.md        # Documentation DB
│
├── src/                        # Code source Java
│   └── main/
│       ├── java/com/planning/
│       │   ├── controller/    # Servlets REST
│       │   ├── dao/           # Accès aux données
│       │   ├── model/         # Entités métier
│       │   └── service/       # Logique métier
│       └── resources/
│           ├── database.properties
│           └── log.properties
│
├── target/                     # Artéfacts de compilation (généré)
│
├── WEB-INF/
│   └── web.xml                # Configuration servlet
│
├── index.html                  # Dashboard principal (point d'entrée racine)
├── pom.xml                     # Configuration Maven
└── README.md                   # Documentation principale
```

## Pages HTML - Architecture Frontend

### Pages Principales

#### 🏠 **index.html** - Tableau de Bord
**Rôle** : Vue d'ensemble centralisée de l'application  
**Fonctionnalités** :
- Compteurs en temps réel (activités, tâches terminées, en cours, contraintes)
- Section Indice de Fatigue avec métriques visuelles (moyenne, max, min, heures/jour)
- Liste des activités récentes
- Aperçu planning hebdomadaire
- Résumé des contraintes actuelles
- Auto-rafraîchissement toutes les minutes

**Dépendances** :
- `assets/css/style.css` : Styles globaux + styles inline pour cards fatigue
- `assets/js/shared.js` : API client, session, toasts
- Endpoints : `/activites`, `/contraintes`, `/planning`, `/stats`, `/fatigue`

---

#### 📋 **activities.html** - Gestion des Activités
**Rôle** : CRUD complet des activités avec détection de conflits  
**Fonctionnalités** :
- Formulaire de création/modification d'activités
- Champs : titre, description, priorité (basse/normale/haute), statut (planifié/en cours/terminé), dates début/fin
- Liste paginée avec actions Modifier/Supprimer
- Alerte visuelle des conflits (critiques/majeures) avec résumé
- Optimisation automatique lors de la création/mise à jour
- Auto-refresh toutes les 30 secondes

**Dépendances** :
- `assets/css/style.css` + styles inline pour alertes
- `assets/js/shared.js`
- Endpoints : `/activites` (GET/POST create/update/delete), `/conflits`

---

#### 🚫 **constraints.html** - Gestion des Contraintes
**Rôle** : Configuration des disponibilités et périodes bloquées  
**Fonctionnalités** :
- **Horaires récurrents** : jour de la semaine, type (indisponible/disponible), plage horaire
- **Périodes bloquées** : motif, dates de début/fin absolues
- Liste unifiée avec distinction visuelle horaires/personnelles
- Suppression individuelle par ID
- Auto-refresh toutes les 30 secondes

**Dépendances** :
- `assets/css/style.css`
- `assets/js/shared.js`
- Endpoint : `/contraintes` (GET, POST create/delete avec scope horaire/personnelle)

---

#### 📅 **planning.html** - Vue Planning Hebdomadaire
**Rôle** : Calendrier hebdomadaire avec visualisation des activités  
**Fonctionnalités** :
- Navigation semaine précédente/suivante
- Grille 7 jours avec activités positionnées par horaire
- Code couleur par priorité (haute=rouge, normale=gris, basse=vert)
- Alerte des conflits de la semaine en cours
- Compteur d'événements total

**Dépendances** :
- `assets/css/style.css` + styles inline calendrier
- `assets/js/shared.js`
- Endpoints : `/activites`, `/conflits`

---

#### ⚙️ **optimisation.html** - Optimisation Automatique
**Rôle** : Génération et application de planning optimisé via Simulated Annealing  
**Fonctionnalités** :
- Sélection plage temporelle (dates début/fin)
- Bouton "Générer Planning Optimisé"
- Affichage résultats : activités replanifiées avec dates suggérées, badges priorité
- Bouton "Appliquer" pour persister les changements en DB
- Compteur d'activités optimisées

**Dépendances** :
- `assets/css/style.css` + styles inline badges
- `assets/js/shared.js`
- Endpoints : `/optimisation/generer`, `/optimisation/appliquer`

---

#### 📊 **stats.html** - Statistiques & Fatigue
**Rôle** : Analyse de la charge de travail et indice de fatigue  
**Fonctionnalités** :
- Sélection période d'analyse (dates)
- Bouton "Analyser"
- Section Résumé : heures totales, activités planifiées/terminées, contraintes actives
- Section Fatigue : moyenne/max/min + badges de risque (bas/moyen/élevé)
- Liste fatigue quotidienne avec scores détaillés

**Dépendances** :
- `assets/css/style.css` + styles inline cards
- `assets/js/shared.js`
- Endpoints : `/stats`, `/fatigue`

---

#### 🔐 **login.html** - Authentification
**Rôle** : Page de connexion/inscription  
**Fonctionnalités** :
- Onglets Connexion/Inscription
- Validation côté client
- Stockage session dans localStorage (userId, username)
- Redirection vers index.html après succès
- Styles inline (pas de dépendance CSS externe)

**Dépendances** :
- Styles inline uniquement
- Endpoint : `/auth/login`, `/auth/register` (via backend)

---

#### 🧪 **add-test-data.html** - Utilitaire de Seeding
**Rôle** : Outil de développement pour générer des données de test  
**Fonctionnalités** :
- Bouton unique "Ajouter les Activités"
- Crée 11 activités prédéfinies pour semaine courante (Dec 22-28, 2025)
- Variété : haute/normale/basse priorité, planifié/en_cours/terminé
- Console de log en temps réel
- Styles inline

**Dépendances** :
- Styles inline uniquement
- Endpoint : `/activites` (POST create)

---

## Ressources Partagées (assets/)

### 🎨 **assets/css/style.css**
**Rôle** : Styles globaux de l'application  
**Contenu** :
- Variables CSS (couleurs, gradients, ombres)
- Layout général (app-shell, sidebar, main)
- Navigation et brand
- Cards et sections
- Formulaires et inputs
- Calendrier
- Stats et fatigue (métriques, barres, badges)
- Animations et transitions
- Responsive design (media queries)

**Utilisation** : Référencé par toutes les pages HTML principales via `<link rel="stylesheet" href="assets/css/style.css" />`

---

### 🔧 **assets/js/shared.js**
**Rôle** : Bibliothèque JavaScript partagée  
**Contenu** :
- **Configuration** : `API_BASE`, `CURRENT_USER_ID` (localStorage)
- **Toasts** : `showToast(message, type)` pour notifications temporaires
- **Session** : `initUserSession()` avec redirection login et bouton déconnexion
- **API Activities** : `loadActivities()`, `createActivity()`, `updateActivity()`, `deleteActivity()`
- **API Constraints** : `loadConstraints()`, `saveMinSleep()`, `addUnavailability()`, `deleteUnavailability()`
- Initialisation automatique (`DOMContentLoaded`)

**Utilisation** : Référencé par toutes les pages HTML principales via `<script src="assets/js/shared.js"></script>`

---

## Backend - Architecture Java

### Controllers (Servlets REST)
- **ActiviteController** : CRUD activités + validation + optimisation auto
- **ContrainteController** : Fetch/create/delete horaires et personnelles
- **ConflitController** : Détection conflits avec sévérité
- **OptimisationController** : Génération et application via Simulated Annealing
- **StatsController** : `/stats` et `/fatigue` avec parsing flexible et locale US
- **AuthController** : Login/register (à finaliser)
- **UserController**, **PlanningController** : Utilitaires

### Services
- **ConflitService** : Détection intersections activités/contraintes
- **OptimisationService** : Algorithme Simulated Annealing pour réorganisation
- **FatigueService** : Calcul indice fatigue (nuit, matins précoces, chevauchements, blocs continus)

### DAO
- **ActiviteDAO** : `findAllByUser`, `findByUserAndDateRange`, `save`, `update`, `delete`
- **ContrainteDAO** : Fetch horaires/personnelles par user
- **UtilisateurDAO** : CRUD utilisateurs
- **PlanningDAO** : Opérations planning (moins utilisé)
- **DBConnection** : Pool connexions JDBC

### Models
- `Activite`, `Categorie`, `ContrainteHoraire`, `ContraintePersonnelle`, `Planning`, `Utilisateur`, etc.

---

## Configuration

### Maven (pom.xml)
- **Plugins** : tomcat7-maven-plugin (port 9090), maven-compiler-plugin (Java 8)
- **Dépendances** : mysql-connector-java 8.0, servlet-api 3.1, jsp-api 2.3, gson 2.10

### Base de Données (database.properties)
```properties
db.url=jdbc:mysql://localhost:3306/planning_db
db.username=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

### Serveur
- **Port** : 9090
- **Context Path** : `/projet-planning-intelligent`
- **Base URL** : `http://localhost:9090/projet-planning-intelligent`

---

## Conventions de Code

### Frontend
- **Nomenclature** : camelCase pour variables JS, kebab-case pour classes CSS
- **API Calls** : Via fonctions `shared.js` (centralisation)
- **Toasts** : `showToast(message, 'success'|'warning'|'error'|'info')`
- **Session** : `localStorage.getItem('userId')` et `username`

### Backend
- **Réponses JSON** : `{ "status": "ok"|"error", "error": "...", ...data }`
- **Dates** : ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss`) côté JSON
- **Locale** : US pour formatage numérique JSON (éviter virgules décimales)
- **Parsing Dates** : Flexible (avec/sans secondes, avec/sans 'T')

---

## Améliorations Futures

### Frontend
- Remplacer localStorage par cookies sécurisés pour session
- Ajouter pagination réelle sur activités/contraintes
- Implémenter drag-and-drop sur planning.html
- Dark mode toggle

### Backend
- Finaliser login DB-based (AuthController + UtilisateurDAO)
- Ajouter cache pour contraintes (Redis/Caffeine)
- Optimiser requêtes SQL (index, N+1)
- Tests unitaires (JUnit + Mockito)

### Infrastructure
- Migration MySQL 8.0+
- CI/CD pipeline (GitHub Actions)
- Docker containerization
- Tomcat 9+ ou migration Spring Boot

---

## Scripts Utiles

### Démarrage Serveur
```bash
mvn clean tomcat7:run
```

### Compilation
```bash
mvn clean compile
```

### Build WAR
```bash
mvn clean package
```

### Test Endpoints
```bash
# Activities
curl "http://localhost:9090/projet-planning-intelligent/activites?userId=2"

# Stats
curl "http://localhost:9090/projet-planning-intelligent/stats?userId=2&dateDebut=2025-12-22T00:00:00&dateFin=2025-12-28T23:59:59"

# Fatigue
curl "http://localhost:9090/projet-planning-intelligent/fatigue?userId=2&dateDebut=2025-12-22T00:00:00&dateFin=2025-12-28T23:59:59"
```

---

**Dernière mise à jour** : 28 décembre 2025  
**Version** : 1.0-SNAPSHOT
