ALTER TABLE parametre_etape
    ADD COLUMN IF NOT EXISTS code_parametre VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_parametre_code ON parametre_etape (code_parametre);
