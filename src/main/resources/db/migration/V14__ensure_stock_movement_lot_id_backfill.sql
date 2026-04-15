-- Ensure stock_movement.lot_id exists, is indexed and backfilled from stock.lot_id when missing.

ALTER TABLE stock_movement
    ADD COLUMN IF NOT EXISTS lot_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_stock_movement_lot_id ON stock_movement (lot_id);

-- Backfill lot_id for historical rows.
UPDATE stock_movement sm
JOIN stock s ON s.id_stock = sm.stock_id
SET sm.lot_id = s.lot_id
WHERE sm.lot_id IS NULL
  AND s.lot_id IS NOT NULL;

-- Add FK only if it does not already exist.
SET @has_fk_mouvement_lot := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock_movement'
      AND CONSTRAINT_NAME = 'fk_mouvement_lot'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql := CASE
    WHEN @has_fk_mouvement_lot = 0
        THEN 'ALTER TABLE stock_movement ADD CONSTRAINT fk_mouvement_lot FOREIGN KEY (lot_id) REFERENCES lot_olives (id_lot)'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
