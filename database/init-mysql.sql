-- Création de la base de données MySQL pour Planning Intelligent
-- Exécutez ce script dans MySQL Workbench ou via ligne de commande

CREATE DATABASE IF NOT EXISTS planning_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE planning_db;

-- Table utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table catégories
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    couleur VARCHAR(7) DEFAULT '#6cc5ff',
    description TEXT
) ENGINE=InnoDB;

-- Table activités
CREATE TABLE IF NOT EXISTS activites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME NOT NULL,
    lieu VARCHAR(200),
    priorite ENUM('BASSE', 'NORMALE', 'HAUTE', 'URGENTE') DEFAULT 'NORMALE',
    statut ENUM('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE') DEFAULT 'PLANIFIE',
    categorie_id INT,
    utilisateur_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (categorie_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table contraintes horaires (règles générales)
CREATE TABLE IF NOT EXISTS contraintes_horaires (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    jour_semaine ENUM('LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI', 'DIMANCHE'),
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    type_contrainte ENUM('DISPONIBLE', 'INDISPONIBLE') DEFAULT 'DISPONIBLE',
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table contraintes personnelles (dates spécifiques)
CREATE TABLE IF NOT EXISTS contraintes_personnelles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME NOT NULL,
    motif VARCHAR(200),
    type_contrainte ENUM('CONGE', 'REUNION', 'AUTRE') DEFAULT 'AUTRE',
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table planning (relation activité-utilisateur si multi-utilisateurs)
CREATE TABLE IF NOT EXISTS plannings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    utilisateur_id INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    description TEXT,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Données de test
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) VALUES
('Admin', 'System', 'admin@planning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye8xFyqQOq3Z5N1c8X3h5dLQz8XYQqLqG', 'ADMIN'),
('Ayouni', 'Rami', 'ramiayouni@planning.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye8xFyqQOq3Z5N1c8X3h5dLQz8XYQqLqG', 'USER');

INSERT INTO categories (nom, couleur, description) VALUES
('Travail', '#6cc5ff', 'Activités professionnelles'),
('Personnel', '#8a5cff', 'Activités personnelles'),
('Sport', '#3ddc97', 'Activités sportives'),
('Formation', '#fcbf49', 'Cours et formations');

INSERT INTO activites (titre, description, date_debut, date_fin, priorite, statut, categorie_id, utilisateur_id) VALUES
('Réunion équipe', 'Point hebdomadaire', '2025-11-18 09:00:00', '2025-11-18 10:30:00', 'HAUTE', 'PLANIFIE', 1, 2),
('Cours Java', 'Formation avancée', '2025-11-19 14:00:00', '2025-11-19 17:00:00', 'NORMALE', 'PLANIFIE', 4, 2),
('Séance sport', 'Musculation', '2025-11-20 18:00:00', '2025-11-20 19:30:00', 'BASSE', 'PLANIFIE', 3, 2);

-- Index pour optimisation
CREATE INDEX idx_activites_dates ON activites(date_debut, date_fin);
CREATE INDEX idx_activites_utilisateur ON activites(utilisateur_id);
CREATE INDEX idx_activites_statut ON activites(statut);
