-- Suppression de la colonne valeur_estimee de la table valeur_reelle_parametre
-- La deviation est maintenant calculee directement a partir de la valeur du parametre du guide.

ALTER TABLE valeur_reelle_parametre
    DROP COLUMN IF EXISTS valeur_estimee;

ALTER TABLE valeur_reelle_parametre
    DROP COLUMN IF EXISTS unite_mesure;