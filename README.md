# 📅 Planner Intelligent - Planification Personnelle Optimisée

Application web complète de gestion de planning avec détection de conflits, optimisation automatique et calcul d'indice de fatigue.

**Backend** : Java 8 + Servlets + MySQL  
**Frontend** : HTML5 + CSS3 + JavaScript Vanilla  
**Build** : Maven + Tomcat 7

---

## 🚀 Démarrage Rapide

```bash
cd planner_app_multi

# Compilation
mvn clean compile

# Lancement serveur (port 9090)
mvn tomcat7:run
```

**Accès application** : http://localhost:9090/projet-planning-intelligent

**Compte démo** :
- Email: `admin@planning.com`
- Mot de passe: `password`

---

## 📁 Structure du Projet

```
planner_app_multi/
├── assets/                     # ✅ Ressources centralisées
│   ├── css/style.css          # Styles globaux
│   └── js/shared.js           # API client et utilitaires
│
├── docs/                       # 📚 Documentation complète
│   ├── PROJECT_ORGANIZATION.md # Structure détaillée du projet
│   ├── STATUS.md               # État actuel et métriques
│   ├── FEATURES.md             # Liste des fonctionnalités
│   ├── GUIDE_UTILISATEUR.md    # Guide utilisateur
│   └── ARCHITECTURE.md         # Architecture technique
│
├── database/                   # 🗄️ Scripts SQL
│   ├── init-mysql.sql         # Schéma initial
│   └── README-MYSQL.md        # Documentation DB
│
├── src/main/                   # ☕ Code source Java
│   ├── java/com/planning/
│   │   ├── controller/        # Servlets REST (7 controllers)
│   │   ├── dao/              # Accès données (5 DAO)
│   │   ├── model/            # Entités métier (8+ models)
│   │   └── service/          # Logique métier (3 services)
│   └── resources/
│       ├── database.properties
│       └── log.properties
│
├── WEB-INF/                    # Configuration web.xml
├── target/                     # Artéfacts de build (généré)
│
├── Pages HTML (frontend)       # 🎨 Interface utilisateur
│   ├── index.html             # Dashboard principal
│   ├── activities.html        # Gestion activités
│   ├── constraints.html       # Gestion contraintes
│   ├── planning.html          # Vue hebdomadaire
│   ├── optimisation.html      # Optimisation automatique
│   ├── stats.html             # Statistiques & fatigue
│   ├── login.html             # Authentification
│   └── add-test-data.html     # Utilitaire de seeding
│
├── pom.xml                     # Configuration Maven
└── README.md                   # Ce fichier
```

---

## ✨ Fonctionnalités Implémentées

### ✅ **Core Features**

#### 1. **Gestion des Activités**
- CRUD complet (création, lecture, mise à jour, suppression)
- Priorités (basse, normale, haute) et statuts (planifié, en cours, terminé)
- Dates début/fin avec validation
- Liste dynamique avec édition inline
- Auto-refresh toutes les 30 secondes

#### 2. **Détection de Conflits**
- Analyse intersections activités-activités et activités-contraintes
- Classification par sévérité (CRITIQUE, MAJEURE, MINEURE)
- Alertes visuelles avec résumé groupé
- Toast notifications en temps réel

#### 3. **Optimisation Automatique**
- Algorithme Simulated Annealing pour réorganisation intelligente
- Minimise conflits tout en respectant priorités et contraintes
- Génération de suggestions non-destructive
- Application explicite par utilisateur

#### 4. **Gestion des Contraintes**
- **Horaires récurrents** : disponibilités par jour de la semaine
- **Périodes bloquées** : absences avec dates absolues
- Formulaires dédiés et liste unifiée
- Intégration dans détection de conflits

#### 5. **Calcul Indice de Fatigue**
- Analyse quotidienne sur période configurable
- Métriques : heures de nuit, matins précoces, chevauchements, blocs continus
- Score 0-100 avec badges de risque (bas/moyen/élevé)
- Résumé : moyenne, max, min, heures/jour

#### 6. **Statistiques et Analyses**
- Dashboard centralisé avec compteurs temps réel
- Activités totales, terminées, en cours
- Contraintes actives
- Visualisations interactives (barres, grilles, badges)

#### 7. **Planning Hebdomadaire**
- Calendrier 7 jours avec navigation
- Code couleur par priorité
- Positionnement par horaire
- Compteur d'événements

#### 8. **Authentification** (Partielle)
- Page login/register fonctionnelle
- Session localStorage (userId, username)
- Redirection automatique si non authentifié
- *Note : Backend AuthController à finaliser*

---

## 📚 Documentation Complète

| Document | Description |
|----------|-------------|
| [📂 PROJECT_ORGANIZATION.md](docs/PROJECT_ORGANIZATION.md) | Structure détaillée fichiers, pages HTML, architecture |
| [📊 STATUS.md](docs/STATUS.md) | État actuel, problèmes résolus, métriques de code |
| [⚙️ FEATURES.md](docs/FEATURES.md) | Liste exhaustive des fonctionnalités |
| [📖 GUIDE_UTILISATEUR.md](docs/GUIDE_UTILISATEUR.md) | Instructions d'utilisation pas à pas |
| [🏗️ ARCHITECTURE.md](docs/ARCHITECTURE.md) | Diagrammes et design patterns |

---

## 🛠️ Technologies et Stack

### Backend
- **Langage** : Java 8
- **Framework** : Servlets 3.1 (sans Spring)
- **DB** : MySQL 5.7+ / 8.0
- **Accès Données** : JDBC pur (DAO pattern)
- **Build** : Maven 3.9+
- **Serveur** : Apache Tomcat 7 (plugin Maven)
- **JSON** : Gson 2.10

### Frontend
- **Langages** : HTML5, CSS3, JavaScript ES6+
- **Pas de framework** : Vanilla JS avec Fetch API
- **Styles** : CSS natif (variables, gradients, flexbox/grid)
- **Session** : localStorage
- **Notifications** : Toasts custom

### Architecture
- **Pattern** : DAO-Service-Controller (3-tier)
- **API** : REST JSON (endpoints `/activites`, `/contraintes`, `/optimisation`, etc.)
- **Sécurité** : Validation côté client + serveur

---

## 🔧 Configuration

### Base de Données

**Fichier** : `src/main/resources/database.properties`

```properties
db.url=jdbc:mysql://localhost:3306/planning_db
db.username=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

### Initialisation

**Créer le schema** :
```bash
mysql -u root -p < database/init-mysql.sql
```

**Tables créées** :
- `utilisateurs` : Comptes utilisateur
- `categories` : Types d'activités
- `activites` : Activités planifiées
- `contraintes_horaires` : Disponibilités récurrentes
- `contraintes_personnelles` : Périodes bloquées
- `plannings` : Associations activités-utilisateurs

---

## 🧪 Données de Test

**Générateur inclus** : http://localhost:9090/projet-planning-intelligent/add-test-data.html

Crée 11 activités variées pour la semaine courante (22-28 décembre 2025) :
- 3 haute priorité, 5 normale, 3 basse
- Statuts mixtes : planifié, en cours, terminé
- Catégories : Études, Sport, Loisirs, Repos

---

## 📋 Commandes Utiles

### Développement
```bash
# Compilation seule
mvn clean compile

# Lancement serveur + watch
mvn tomcat7:run

# Build WAR pour déploiement
mvn clean package
```

### Tests Endpoints
```bash
# Liste activités utilisateur 2
curl "http://localhost:9090/projet-planning-intelligent/activites?userId=2"

# Statistiques semaine courante
curl "http://localhost:9090/projet-planning-intelligent/stats?userId=2&dateDebut=2025-12-22T00:00:00&dateFin=2025-12-28T23:59:59"

# Indice fatigue 7 derniers jours
curl "http://localhost:9090/projet-planning-intelligent/fatigue?userId=2&dateDebut=2025-12-22T00:00:00&dateFin=2025-12-28T23:59:59"

# Conflits détectés
curl "http://localhost:9090/projet-planning-intelligent/conflits?userId=2&start=2025-12-22&end=2025-12-28"
```

---

## 📊 État du Projet

**Version** : 1.0-SNAPSHOT  
**Statut Compilation** : ✅ BUILD SUCCESS  
**Statut Fonctionnel** : ✅ Opérationnel (90% features)  
**Dernière Vérification** : 28 Décembre 2025

### Compteurs de Code
- **Backend** : ~3000+ lignes Java (27 fichiers)
- **Frontend** : ~1500 lignes HTML/CSS/JS (8 pages)
- **Documentation** : 6 fichiers Markdown complets

### Problèmes Résolus Récemment
- ✅ Compteurs dashboard (terminées/en cours) via timestamps
- ✅ Stats JSON invalide (locale US pour décimales)
- ✅ Fatigue endpoint vide (parsing dates flexible)
- ✅ Duplicate ConflitController supprimé
- ✅ Jours français mappés vers DayOfWeek Java
- ✅ Organisation assets centralisée

---

## 🔮 Améliorations Futures

### Court Terme
- [ ] Finaliser AuthController avec binding DB
- [ ] Remplacer localStorage par cookies sécurisés
- [ ] Tests unitaires backend (JUnit + Mockito)
- [ ] Pagination backend pour activités

### Moyen Terme
- [ ] Migration Spring Boot
- [ ] Cache Redis pour contraintes et stats
- [ ] API REST normalisée (OpenAPI 3.0)
- [ ] Frontend React ou Vue.js

### Long Terme
- [ ] Multi-utilisateurs avec rôles (admin/user)
- [ ] Notifications push (WebSocket)
- [ ] Synchronisation calendrier externe (Google, Outlook)
- [ ] Machine Learning pour prédiction fatigue

---

## 📞 Support et Contribution

**Statut Projet** : Académique (Mini-Projet Génie Logiciel)  
**Licence** : Non spécifiée (projet éducatif)

**Documentation Complète** : Voir dossier `docs/`  
**Questions** : Consulter [STATUS.md](docs/STATUS.md) pour état détaillé

---

**Dernière Mise à Jour** : 28 Décembre 2025  
**Auteurs** : Équipe Planner Intelligent
