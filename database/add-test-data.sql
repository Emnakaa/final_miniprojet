-- Ajouter des activités pour la semaine courante (décembre 22-28, 2025)
USE planning_db;

INSERT INTO activites (titre, description, date_debut, date_fin, priorite, statut, categorie_id, utilisateur_id) VALUES
('Rapport mensuel', 'Synthèse décembre', '2025-12-22 10:00:00', '2025-12-22 12:00:00', 'HAUTE', 'EN_COURS', 1, 2),
('Réunion client', 'Présentation projet', '2025-12-23 14:00:00', '2025-12-23 15:30:00', 'HAUTE', 'PLANIFIE', 1, 2),
('Formation SQL', 'Optimisation requêtes', '2025-12-24 09:00:00', '2025-12-24 12:00:00', 'NORMALE', 'PLANIFIE', 4, 2),
('Gym', 'Session cardio', '2025-12-24 18:00:00', '2025-12-24 19:00:00', 'BASSE', 'PLANIFIE', 3, 2),
('Révision code', 'Review tickets', '2025-12-25 09:00:00', '2025-12-25 11:00:00', 'NORMALE', 'TERMINE', 1, 2),
('Réunion équipe', 'Standup', '2025-12-26 09:30:00', '2025-12-26 10:00:00', 'HAUTE', 'TERMINE', 1, 2),
('Documentation API', 'API REST endpoints', '2025-12-26 14:00:00', '2025-12-26 17:00:00', 'NORMALE', 'EN_COURS', 1, 2),
('Yoga', 'Séance relaxation', '2025-12-27 17:00:00', '2025-12-27 18:00:00', 'BASSE', 'PLANIFIE', 3, 2),
('Réunion planning', 'Sprint planning', '2025-12-28 09:00:00', '2025-12-28 10:30:00', 'HAUTE', 'PLANIFIE', 1, 2),
('Développement feature', 'Fatigue index', '2025-12-28 11:00:00', '2025-12-28 14:00:00', 'HAUTE', 'EN_COURS', 1, 2),
('Test unitaire', 'Couverture 80%', '2025-12-28 14:30:00', '2025-12-28 16:00:00', 'NORMALE', 'PLANIFIE', 1, 2);
