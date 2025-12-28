# Planner - Gestion Intelligente du Temps

Bienvenue dans Planner, une application web pour gérer vos activités, contraintes et planification.

## 🚀 Démarrage Rapide

### 1. **Accéder à l'Application**
- Ouvrir un navigateur et aller à: `http://localhost:9090/projet-planning-intelligent/login.html`

### 2. **Authentification**

#### Connexion
- Email: `admin@planning.com`
- Mot de passe: `password`

#### Créer un Compte
- Cliquez sur l'onglet "Créer un compte"
- Remplissez votre nom, prénom, email et mot de passe
- Le compte sera créé automatiquement

### 3. **Navigation Principales**

#### 📊 Tableau de Bord (index.html)
- Vue d'ensemble de tous vos activités
- Statistiques: Nombre d'activités, tâches terminées, contraintes, tâches en cours
- Aperçu de la semaine actuelle
- Raccourcis rapides vers les autres pages

#### ✓ Activités (activities.html)
- **Créer une activité**: Remplissez le formulaire avec:
  - Titre (obligatoire)
  - Description (optionnel)
  - Priorité (Basse, Normale, Haute)
  - Statut (Planifié, En cours, Terminé)
  - Date et heure de début/fin
- **Modifier**: Cliquez sur l'icône ✏️ pour éditer
- **Supprimer**: Cliquez sur l'icône 🗑️ pour supprimer

#### ⏰ Contraintes (constraints.html)
- **Horaires Récurrents**: Définissez vos disponibilités par jour de la semaine
- **Périodes Bloquées**: Marquez les périodes où vous n'êtes pas disponible (maladie, congés, etc.)

#### 📆 Planning (planning.html)
- **Vue Hebdomadaire**: Visualisez toutes vos activités de la semaine
- **Couleurs**: Les activités sont groupées par jour
- **Navigation**: Utilisez les boutons pour voir les semaines précédentes/suivantes

## 🔐 Authentification

- Les identifiants sont stockés de façon sécurisée en base de données
- Les sessions sont gérées via localStorage
- Vous êtes automatiquement redirigé vers login.html si vous n'êtes pas connecté

## 🏗️ Architecture Technique

### Backend
- **Serveur**: Apache Tomcat 7 (port 9090)
- **Langage**: Java 8
- **Framework**: Servlets (Jakarta EE)
- **Base de Données**: MySQL 5.7+

### Frontend
- **HTML5**, **CSS3**, **JavaScript (Vanilla)**
- **API Communication**: Fetch API avec JSON
- **Responsive Design**: Adapté à tous les écrans

### Endpoints API

- `POST /auth` - Authentification (login/register)
- `GET /activites` - Récupérer les activités
- `POST /activites` - Créer/modifier/supprimer une activité
- `GET /contraintes` - Récupérer les contraintes
- `POST /contraintes` - Ajouter/supprimer des contraintes
- `GET /planning` - Récupérer le planning hebdomadaire

## 💾 Données

### Utilisateurs
- Email, nom, prénom, mot de passe (hashé)

### Activités
- Titre, description, date de début/fin
- Priorité (BASSE, NORMALE, HAUTE)
- Statut (PLANIFIE, EN_COURS, TERMINE)

### Contraintes
- **Horaires**: Jour de la semaine, heures de début/fin, type
- **Personnelles**: Période, motif, type

## 🔧 Dépannage

### Le serveur ne répond pas
1. Vérifier que Tomcat est en cours d'exécution
2. Vérifier le port 9090: `mvn tomcat7:run` dans le dossier du projet
3. Vérifier que MySQL est accessible

### Erreur de connexion à la base de données
1. Vérifier que MySQL est démarré
2. Vérifier les credentials dans `src/main/resources/database.properties`
3. Vérifier que la base `planning_db` existe

### Les données ne se sauvegardent pas
1. Vérifier que les requêtes POST retournent `{"status":"ok"}`
2. Consulter la console du navigateur pour les erreurs
3. Vérifier les logs Tomcat

## 📝 Fonctionnalités Principales

✅ Authentification sécurisée  
✅ Gestion complète des activités (CRUD)  
✅ Système de contraintes horaires et personnelles  
✅ Vue hebdomadaire du planning  
✅ Dashboard avec statistiques  
✅ Interface utilisateur intuitive et moderne  
✅ Synchronisation en temps réel  

## 🎯 Cas d'Usage

1. **Organiser votre semaine**
   - Créez vos activités avec des dates/heures précises
   - Visualisez-les dans le planning hebdomadaire
   - Ajustez les priorités selon vos besoins

2. **Gérer vos contraintes**
   - Définissez vos plages horaires de disponibilité
   - Marquez les périodes où vous êtes absent
   - Le système en tiendra compte dans la planification

3. **Suivre votre progression**
   - Marquez les activités comme terminées
   - Consultez les statistiques du tableau de bord
   - Visualisez vos accomplissements

## 📞 Support

Pour toute question ou problème, consultez les logs:
- **Tomcat**: `target/tomcat/logs/`
- **Navigateur**: Appuyez sur F12 pour ouvrir la console développeur

---

**Planner** - Gestion intelligente du temps | Version 1.0
