-- V26: create fournisseur table and backfill lot_olives

CREATE TABLE IF NOT EXISTS fournisseur (
    id_fournisseur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255),
    cin VARCHAR(255) NOT NULL,
    CONSTRAINT uk_fournisseur_cin UNIQUE (cin)
);

-- Insert distinct fournisseurs from existing lot_olives (skip empty/null CIN)
INSERT INTO fournisseur (nom, cin)
SELECT DISTINCT
    COALESCE(fournisseur_nom, ''), TRIM(fournisseur_cin)
FROM lot_olives
WHERE fournisseur_cin IS NOT NULL AND TRIM(fournisseur_cin) <> ''
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- Add fournisseur_id to lot_olives if missing
ALTER TABLE lot_olives
    ADD COLUMN IF NOT EXISTS fournisseur_id BIGINT NULL;

-- Backfill lot_olives.fournisseur_id by matching CIN
UPDATE lot_olives l
JOIN fournisseur f ON TRIM(l.fournisseur_cin) = TRIM(f.cin)
SET l.fournisseur_id = f.id_fournisseur
WHERE l.fournisseur_cin IS NOT NULL AND TRIM(l.fournisseur_cin) <> '' AND (l.fournisseur_id IS NULL OR l.fournisseur_id = 0);

-- Add foreign key constraint
ALTER TABLE lot_olives
    ADD CONSTRAINT IF NOT EXISTS fk_lot_fournisseur
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id_fournisseur);

-- Note: keep fournisseur_nom and fournisseur_cin columns for backward compat; they can be removed later after verification.
