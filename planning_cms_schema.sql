-- Table pour le planning CMS (saisie par l'utilisateur PRODUCTION)
CREATE TABLE IF NOT EXISTS `planning_cms` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `cms` VARCHAR(10) NOT NULL,
  `semaine` INT NOT NULL,
  `annee` INT NOT NULL,
  `article` VARCHAR(100),
  `ordre` VARCHAR(50),
  `st_util` VARCHAR(50),
  `ind` VARCHAR(10),
  `qte` DOUBLE DEFAULT 0,
  `cad` DOUBLE DEFAULT 0,
  `nbre_h` DOUBLE DEFAULT 0,
  `jalonnement` VARCHAR(20),
  `lundi` DOUBLE DEFAULT 0,
  `mardi` DOUBLE DEFAULT 0,
  `mercredi` DOUBLE DEFAULT 0,
  `jeudi` DOUBLE DEFAULT 0,
  `vendredi` DOUBLE DEFAULT 0,
  `samedi` DOUBLE DEFAULT 0,
  `commentaire` VARCHAR(255),
  `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);