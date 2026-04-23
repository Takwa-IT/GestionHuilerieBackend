-- Création de la table prediction pour stocker les résultats de prédiction ML
CREATE TABLE IF NOT EXISTS prediction (
                                          id_prediction BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          mode_prediction VARCHAR(50) NOT NULL COMMENT 'Mode de prédiction utilisé (with_lab, no_lab)',
    qualite_predite VARCHAR(100) COMMENT 'Classe de qualité prédite par le modèle',
    probabilite_qualite DECIMAL(5,4) COMMENT 'Probabilité associée à la qualité prédite (0 à 1)',
    rendement_predit_pourcent DECIMAL(5,2) COMMENT 'Rendement d\'huile prédit en pourcentage',
    quantite_huile_recalculee_litres DECIMAL(10,2) COMMENT 'Quantité d\'huile recalculée en litres',
    execution_production_id BIGINT NOT NULL,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prediction_execution_production FOREIGN KEY (execution_production_id) REFERENCES execution_production (id_execution_production) ON DELETE CASCADE,
    CONSTRAINT uk_prediction_execution UNIQUE (execution_production_id)
);

CREATE INDEX IF NOT EXISTS idx_prediction_execution_id ON prediction (execution_production_id);

