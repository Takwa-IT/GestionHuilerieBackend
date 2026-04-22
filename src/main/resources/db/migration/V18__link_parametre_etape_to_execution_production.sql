ALTER TABLE parametre_etape
    ADD COLUMN IF NOT EXISTS execution_production_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_parametre_execution_id ON parametre_etape (execution_production_id);
