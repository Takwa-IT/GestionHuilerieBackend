ALTER TABLE lot_olives
    ADD COLUMN IF NOT EXISTS humidite_pourcent DOUBLE,
    ADD COLUMN IF NOT EXISTS acidite_olives_pourcent DOUBLE,
    ADD COLUMN IF NOT EXISTS taux_feuilles_pourcent DOUBLE;