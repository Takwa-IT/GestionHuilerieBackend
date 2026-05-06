-- Suppression des champs AI liés à la prédiction de la table execution_production
-- Cette migration supprime les colonnes qui ont été extraites de l'entité ExecutionProduction

ALTER TABLE execution_production DROP COLUMN IF EXISTS region;
ALTER TABLE execution_production DROP COLUMN IF EXISTS methode_recolte;
ALTER TABLE execution_production DROP COLUMN IF EXISTS type_sol;
ALTER TABLE execution_production DROP COLUMN IF EXISTS temperature_malaxagec;
ALTER TABLE execution_production DROP COLUMN IF EXISTS duree_malaxage_min;
ALTER TABLE execution_production DROP COLUMN IF EXISTS vitesse_decanteur_tr_min;
ALTER TABLE execution_production DROP COLUMN IF EXISTS rendement;
ALTER TABLE execution_production DROP COLUMN IF EXISTS humidite_pourcent;
ALTER TABLE execution_production DROP COLUMN IF EXISTS acidite_olives_pourcent;
ALTER TABLE execution_production DROP COLUMN IF EXISTS taux_feuilles_pourcent;
ALTER TABLE execution_production DROP COLUMN IF EXISTS pression_extraction_bar;
ALTER TABLE execution_production DROP COLUMN IF EXISTS presence_separateur;
ALTER TABLE execution_production DROP COLUMN IF EXISTS presence_ajout_eau;
