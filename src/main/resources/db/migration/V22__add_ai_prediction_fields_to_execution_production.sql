-- Ajout des champs IA pour la prédiction à la table execution_production
-- Ces champs stockent les paramètres des olives nécessaires pour l'orchestration IA

ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS region VARCHAR(255) COMMENT 'Région d\'origine des olives';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS methode_recolte VARCHAR(255) COMMENT 'Méthode de récolte des olives';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS type_sol VARCHAR(255) COMMENT 'Type de sol de culture';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS temperature_malaxage_c DECIMAL(10,2) COMMENT 'Température de malaxage en Celsius';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS duree_malaxage_min DECIMAL(10,2) COMMENT 'Durée du malaxage en minutes';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS vitesse_decanteur_tr_min DECIMAL(10,2) COMMENT 'Vitesse du décanteur en tours/minute';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS humidite_pourcent DECIMAL(10,2) COMMENT 'Humidité en pourcentage';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS acidite_olives_pourcent DECIMAL(10,2) COMMENT 'Acidité des olives en pourcentage';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS taux_feuilles_pourcent DECIMAL(10,2) COMMENT 'Taux de feuilles en pourcentage';
ALTER TABLE execution_production ADD COLUMN IF NOT EXISTS pression_extraction_bar DECIMAL(10,2) COMMENT 'Pression d\'extraction en bar';
