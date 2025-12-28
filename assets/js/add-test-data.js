// Simple JS to add test data via API calls
const API_BASE = 'http://localhost:9090/projet-planning-intelligent';

const activities = [
  { titre: 'Rapport mensuel', description: 'Synthèse décembre', debut: '2025-12-22T10:00:00', fin: '2025-12-22T12:00:00', priorite: 'HAUTE', statut: 'EN_COURS', userId: 2 },
  { titre: 'Réunion client', description: 'Présentation projet', debut: '2025-12-23T14:00:00', fin: '2025-12-23T15:30:00', priorite: 'HAUTE', statut: 'PLANIFIE', userId: 2 },
  { titre: 'Formation SQL', description: 'Optimisation requêtes', debut: '2025-12-24T09:00:00', fin: '2025-12-24T12:00:00', priorite: 'NORMALE', statut: 'PLANIFIE', userId: 2 },
  { titre: 'Gym', description: 'Session cardio', debut: '2025-12-24T18:00:00', fin: '2025-12-24T19:00:00', priorite: 'BASSE', statut: 'PLANIFIE', userId: 2 },
  { titre: 'Révision code', description: 'Review tickets', debut: '2025-12-25T09:00:00', fin: '2025-12-25T11:00:00', priorite: 'NORMALE', statut: 'TERMINE', userId: 2 },
  { titre: 'Réunion équipe', description: 'Standup', debut: '2025-12-26T09:30:00', fin: '2025-12-26T10:00:00', priorite: 'HAUTE', statut: 'TERMINE', userId: 2 },
  { titre: 'Documentation API', description: 'API REST endpoints', debut: '2025-12-26T14:00:00', fin: '2025-12-26T17:00:00', priorite: 'NORMALE', statut: 'EN_COURS', userId: 2 },
  { titre: 'Yoga', description: 'Séance relaxation', debut: '2025-12-27T17:00:00', fin: '2025-12-27T18:00:00', priorite: 'BASSE', statut: 'PLANIFIE', userId: 2 },
  { titre: 'Réunion planning', description: 'Sprint planning', debut: '2025-12-28T09:00:00', fin: '2025-12-28T10:30:00', priorite: 'HAUTE', statut: 'PLANIFIE', userId: 2 },
  { titre: 'Développement feature', description: 'Fatigue index', debut: '2025-12-28T11:00:00', fin: '2025-12-28T14:00:00', priorite: 'HAUTE', statut: 'EN_COURS', userId: 2 },
  { titre: 'Test unitaire', description: 'Couverture 80%', debut: '2025-12-28T14:30:00', fin: '2025-12-28T16:00:00', priorite: 'NORMALE', statut: 'PLANIFIE', userId: 2 }
];

async function addAllActivities() {
  for (const a of activities) {
    const params = new URLSearchParams({
      action: 'create',
      userId: a.userId,
      titre: a.titre,
      description: a.description,
      debut: a.debut,
      fin: a.fin,
      priorite: a.priorite,
      statut: a.statut
    });
    
    try {
      const resp = await fetch(`${API_BASE}/activites`, {
        method: 'POST',
        body: params,
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      });
      const result = await resp.json();
      console.log(`✓ ${a.titre}`, result);
    } catch (e) {
      console.error(`✗ ${a.titre}:`, e);
    }
  }
  console.log('Done!');
}

addAllActivities();
