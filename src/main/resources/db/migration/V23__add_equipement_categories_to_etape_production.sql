-- Ajout des catégories d'équipements à la table etape_production
-- Ce champ stocke les catégories d'équipements disponibles pour chaque étape
-- Format: JSON array de strings (ex: ["nettoyage", "ajout_eau"])

ALTER TABLE etape_production ADD COLUMN IF NOT EXISTS equipement_categories JSON COMMENT 'Catégories d\'équipements disponibles pour l\'étape';
