
-- Table utilisateur
CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('OPERATEUR', 'TECHNICIEN', 'INGENIEUR') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table historique_pochoir
CREATE TABLE IF NOT EXISTS historique_pochoir (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pouchoir_ref VARCHAR(50) NOT NULL,
    action ENUM('SORTIE', 'RETOUR') NOT NULL,
    date_sortie TIMESTAMP NOT NULL,
    date_retour TIMESTAMP NULL,
    operateur_id INT NOT NULL,
    localisation VARCHAR(100),
    raison VARCHAR(100),
    etat_retour VARCHAR(50),
    remarques TEXT,
    FOREIGN KEY (operateur_id) REFERENCES utilisateur(id),
    INDEX idx_pouchoir_ref (pouchoir_ref),
    INDEX idx_date_sortie (date_sortie),
    INDEX idx_operateur (operateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table audit_log
CREATE TABLE IF NOT EXISTS audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT,
    action VARCHAR(50) NOT NULL,
    table_affectee VARCHAR(50),
    enregistrement_id VARCHAR(50),
    details TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_table (table_affectee),
    INDEX idx_date_action (date_action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


