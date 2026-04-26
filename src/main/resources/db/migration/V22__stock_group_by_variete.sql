ALTER TABLE stock ADD COLUMN IF NOT EXISTS variete VARCHAR(255);

UPDATE stock s
JOIN lot_olives l ON l.id_lot = s.lot_id
SET s.variete = LOWER(TRIM(l.variete))
WHERE s.variete IS NULL;