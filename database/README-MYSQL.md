# Guide d'installation MySQL pour Planning Intelligent

## 1. Installation MySQL

### Option A : XAMPP (recommandé pour Windows)
1. Téléchargez XAMPP : https://www.apachefriends.org/
2. Installez et démarrez MySQL via le panneau XAMPP
3. MySQL tourne sur `localhost:3306` par défaut

### Option B : MySQL Community Server
1. Téléchargez : https://dev.mysql.com/downloads/mysql/
2. Installez avec le mot de passe root : `root`
3. Démarrez le service MySQL

## 2. Créer la base de données

### Via ligne de commande :
```bash
mysql -u root -p
# Entrez le mot de passe : root
```

Puis exécutez :
```sql
source C:/apache-tomcat-9.0.112-src/webapps/mini-projet/database/init-mysql.sql
```

### Via MySQL Workbench :
1. Ouvrez MySQL Workbench
2. Connectez-vous (user: root, password: root)
3. Ouvrez le fichier `database/init-mysql.sql`
4. Cliquez sur ⚡ Execute

## 3. Configuration actuelle

**Fichier:** `src/main/resources/database.properties`
```properties
db.url=jdbc:mysql://localhost:3306/planning_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=root
```

**Si votre configuration diffère :**
- Port différent : changez `3306`
- Autre utilisateur : modifiez `db.user`
- Autre mot de passe : modifiez `db.password`

## 4. Redémarrer l'application

```powershell
cd C:\apache-tomcat-9.0.112-src\webapps\mini-projet
mvn clean package tomcat7:run-war
```

L'application se connectera automatiquement à MySQL sur http://localhost:8082/

## 5. Données de test

**Utilisateurs créés :**
- Email : `admin@planning.com` / Mot de passe : `password`
- Email : `jean.dupont@planning.com` / Mot de passe : `password`

**Catégories :** Travail, Personnel, Sport, Formation
**Activités :** 3 exemples créés pour Jean Dupont

## 6. Vérifier la connexion

Dans MySQL :
```sql
USE planning_db;
SHOW TABLES;
SELECT * FROM utilisateurs;
SELECT * FROM activites;
```
