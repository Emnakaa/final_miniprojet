# Gestion des Contraintes et Détection des Conflits

## ✅ Implémentation Complétée

### 1. Modèle de Conflit
**Fichier:** `src/main/java/com/planning/model/Conflit.java`

Classe représentant un conflit détecté avec:
- Type: CHEVAUCHEMENT, CONTRAINTE_HORAIRE, CONTRAINTE_PERSONNELLE, SOMMEIL
- Sévérité: CRITIQUE, MAJEURE, MINEURE
- Détails: activités impliquées, dates, description

### 2. Service de Détection de Conflits
**Fichier:** `src/main/java/com/planning/service/ConflitService.java`

#### Fonctionnalités:
- ✅ **Détection des chevauchements** entre activités du même utilisateur
- ✅ **Vérification des contraintes horaires** récurrentes (INDISPONIBLE)
- ✅ **Vérification des contraintes personnelles** (périodes bloquées)
- ✅ **Validation avant création/modification** d'activité
- ✅ **Messages d'erreur descriptifs** pour chaque type de conflit

#### Méthodes principales:
```java
// Détecter tous les conflits dans une période
List<Conflit> detecterConflits(int utilisateurId, LocalDateTime debut, LocalDateTime fin)

// Valider une activité avant sauvegarde
List<String> validerActivite(Activite activite, int utilisateurId)
```

### 3. Contrôleur REST
**Fichier:** `src/main/java/com/planning/controller/ConflitController.java`

**Endpoint:** `GET /conflits`

**Paramètres:**
- `userId` (requis): ID de l'utilisateur
- `start` (optionnel): Date début format YYYY-MM-DD
- `end` (optionnel): Date fin format YYYY-MM-DD

**Réponse JSON:**
```json
{
  "status": "ok",
  "conflits": [
    {
      "type": "CHEVAUCHEMENT",
      "severite": "CRITIQUE",
      "description": "Les activités 'Réunion' et 'Formation' se chevauchent...",
      "activite1Id": 5,
      "activite1Titre": "Réunion",
      "activite2Id": 7,
      "activite2Titre": "Formation",
      "dateDebut": "2025-12-28T09:00",
      "dateFin": "2025-12-28T10:30"
    }
  ],
  "count": 1
}
```

### 4. Validation Intégrée
**Fichier:** `src/main/java/com/planning/controller/ActiviteController.java`

Le contrôleur d'activités valide maintenant automatiquement:
- Chevauchements avec d'autres activités
- Respect des contraintes horaires
- Respect des périodes bloquées

**Avant création/modification**, le système retourne des erreurs si des conflits sont détectés.

### 5. Extension DAO
**Fichiers:** 
- `src/main/java/com/planning/dao/ActiviteDAO.java`
- `src/main/java/com/planning/dao/impl/ActiviteDAOImpl.java`

Nouvelle méthode ajoutée:
```java
List<Activite> findByUserAndDateRange(int userId, LocalDateTime debut, LocalDateTime fin)
```

## 🎯 Types de Conflits Détectés

### 1. Chevauchement d'Activités (CRITIQUE)
Deux activités du même utilisateur se superposent dans le temps.

**Exemple:**
- Activité A: 09:00 - 10:30
- Activité B: 10:00 - 11:00
- ❌ Conflit: 30 minutes de chevauchement

### 2. Violation de Contrainte Horaire (MAJEURE)
Une activité planifiée pendant une plage horaire marquée INDISPONIBLE.

**Exemple:**
- Contrainte: Lundi 00:00-08:00 INDISPONIBLE (sommeil)
- Activité: Lundi 07:00-09:00
- ❌ Conflit: 1h de chevauchement avec période indisponible

### 3. Violation de Contrainte Personnelle (MAJEURE)
Une activité planifiée pendant une période bloquée (congé, réunion, etc.).

**Exemple:**
- Contrainte: 2025-12-24 au 2025-12-26 (Vacances)
- Activité: 2025-12-25 14:00-16:00
- ❌ Conflit: activité pendant les vacances

## 📊 Utilisation Frontend

### Récupérer les conflits
```javascript
const userId = getCurrentUserId();
const start = '2025-12-28';
const end = '2026-01-04';

const response = await fetch(
  `${API_BASE}/conflits?userId=${userId}&start=${start}&end=${end}`
);
const data = await response.json();

if (data.status === 'ok') {
  data.conflits.forEach(conflit => {
    console.log(`${conflit.severite}: ${conflit.description}`);
  });
}
```

### Gérer les erreurs lors de la création
```javascript
const params = new URLSearchParams({
  action: 'create',
  userId: userId,
  titre: 'Nouvelle activité',
  debut: '2025-12-28T09:00',
  fin: '2025-12-28T10:30',
  // ...
});

const response = await fetch(`${API_BASE}/activites`, {
  method: 'POST',
  body: params,
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
});

const result = await response.json();

if (result.status === 'error') {
  alert('Conflits détectés: ' + result.error);
  // Afficher les conflits à l'utilisateur
}
```

## 🧪 Scénarios de Test

### Scénario 1: Chevauchement Simple
1. Créer activité A: 28/12/2025 09:00-11:00
2. Tenter de créer activité B: 28/12/2025 10:00-12:00
3. ✅ Système doit refuser avec message de chevauchement

### Scénario 2: Contrainte Horaire
1. Définir contrainte: Lundi 22:00-07:00 INDISPONIBLE
2. Tenter activité: Lundi 06:00-08:00
3. ✅ Système doit signaler violation de contrainte horaire

### Scénario 3: Période Bloquée
1. Définir congé: 24/12 au 26/12
2. Tenter activité: 25/12 14:00-16:00
3. ✅ Système doit refuser (période bloquée)

### Scénario 4: Consultation des Conflits
1. Créer plusieurs activités avec chevauchements volontaires (mode admin)
2. Appeler `GET /conflits?userId=X&start=...&end=...`
3. ✅ Recevoir liste complète avec détails

## 🔧 Configuration & Extensions Futures

### Priorités de Conflits
Actuellement: tous les chevauchements sont critiques.
**TODO:** Permettre écrasement si priorité plus haute.

### Sommeil Minimal
Structure en place pour vérifier respect du sommeil minimal (8h/nuit).
**TODO:** Implémenter `verifierSommeilMinimal()` dans ConflitService.

### Conflits avec Durée
**TODO:** Afficher durée exacte du chevauchement (en minutes).

### Notifications
**TODO:** Envoyer alertes automatiques quand nouveaux conflits détectés.

## 📝 Notes Techniques

### Performance
- Les requêtes sont optimisées avec des index sur `date_debut` et `date_fin`
- La détection utilise un algorithme O(n²) pour les chevauchements
- Pour > 1000 activités, envisager une structure d'interval tree

### Transactions
Les validations s'effectuent AVANT insertion en base.
Pas de rollback nécessaire puisque rejet en amont.

### Extensibilité
Le système de détection est modulaire:
- Ajouter nouveau type de conflit = nouvelle méthode privée
- Tous les conflits remontés via même endpoint

---

**Statut:** ✅ Fonctionnel et testé  
**Prochaine étape:** Intégration frontend pour affichage visuel des conflits dans le planning
