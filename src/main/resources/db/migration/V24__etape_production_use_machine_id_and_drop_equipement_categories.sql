-- Migration du modele etape_production:
-- - utiliser une relation directe etape -> machine (machine_id)
-- - supprimer l'ancien champ equipement_categories

ALTER TABLE etape_production
    ADD COLUMN IF NOT EXISTS machine_id BIGINT NULL;

UPDATE etape_production ep
LEFT JOIN (
    SELECT etape_production_id, MIN(machine_id) AS machine_id
    FROM etape_production_machine
    GROUP BY etape_production_id
) epm ON epm.etape_production_id = ep.id_etape_production
SET ep.machine_id = COALESCE(ep.machine_id, epm.machine_id)
WHERE ep.machine_id IS NULL;

ALTER TABLE etape_production
    ADD CONSTRAINT fk_etape_production_machine
    FOREIGN KEY (machine_id) REFERENCES machine(id_machine);

ALTER TABLE etape_production
    DROP COLUMN IF EXISTS equipement_categories;

DROP TABLE IF EXISTS etape_production_machine;
