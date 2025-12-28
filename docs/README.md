# 📅 Planner IA - Système de Planification Personnelle Intelligente

Un système complet de gestion d'emploi du temps avec optimisation d'horaire basée sur des contraintes personnelles.

## 🎯 Objectif

Aider les utilisateurs à **planifier efficacement leur semaine** en :
- 📝 Gérant leurs activités et tâches
- ⚙️ Définissant leurs contraintes (sommeil, indisponibilités)
- 🔄 Optimisant automatiquement leur emploi du temps
- 📊 Analysant leur charge de travail et bien-être

## 📦 Stack Technologique

### Frontend
- **HTML5** - Structure et contenu
- **CSS3** - Styles modernes et responsive
- **JavaScript (Vanilla)** - Logique et interactivité
- **Fetch API** - Communication avec le backend

### Backend
- **Java 8+** - Langage principal
- **Apache Tomcat** - Serveur d'application
- **MySQL/MariaDB** - Base de données relationnelle
- **Maven** - Gestion des dépendances et build
- **JDBC** - Accès à la base de données

## 🚀 Démarrage Rapide

### 1️⃣ Installation
```bash
# Vérifier les prérequis
java -version
mvn --version
mysql --version
```

### 2️⃣ Configuration
```bash
# Créer la base de données
mysql -u root -p planning_db < database/init-mysql.sql

# Mettre à jour database.properties
# src/main/resources/database.properties
# db.url=jdbc:mysql://localhost:3306/planning_db
# db.user=root
# db.password=votre_mot_de_passe
```

### 3️⃣ Compilation & Déploiement
```bash
# Compiler et créer le WAR
mvn clean package

# Lancer le serveur
mvn tomcat7:run
```

### 4️⃣ Accès
```
URL : http://localhost:8080/projet-planning-intelligent/login.html
Utilisateur : user
Mot de passe : pass
```

## 📚 Documentation

| Document | Contenu |
|----------|---------|
| **[SETUP.md](SETUP.md)** | 🚀 Démarrage en 5 étapes (LIRE D'ABORD!) |
| **[INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md)** | 🔧 Documentation technique complète |
| **[CHANGELOG.md](CHANGELOG.md)** | 📝 Historique des changements |

## ⚡ Commandes Essentielles

```bash
# Compiler le projet
mvn clean compile

# Créer le WAR
mvn package

# Lancer le serveur avec Tomcat
mvn tomcat7:run

# Lancer en mode debug
mvn -X tomcat7:run
```

## 🔐 Identifiants par Défaut

| Champ | Valeur |
|-------|--------|
| Utilisateur | `user` |
| Mot de passe | `pass` |

## ⚙️ Prérequis

- **Java** : JDK 8+
- **Maven** : 3.6+
- **MySQL** : 5.7+ ou MariaDB
- **Navigateur** : Chrome, Firefox, Safari, Edge

---

**Prêt à commencer ? 👉 Consultez [SETUP.md](SETUP.md) !**