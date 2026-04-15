-- Align legacy reference_id naming with lot_id in DB.
-- This migration is defensive and only runs changes when columns exist.

-- STOCK: reference_id -> lot_id
SET @has_stock_reference_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock'
      AND COLUMN_NAME = 'reference_id'
);
SET @has_stock_lot_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock'
      AND COLUMN_NAME = 'lot_id'
);

SET @sql := CASE
    WHEN @has_stock_reference_id = 1 AND @has_stock_lot_id = 0
        THEN 'ALTER TABLE stock RENAME COLUMN reference_id TO lot_id'
    WHEN @has_stock_reference_id = 1 AND @has_stock_lot_id = 1
        THEN 'UPDATE stock SET lot_id = COALESCE(lot_id, reference_id) WHERE reference_id IS NOT NULL'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := CASE
    WHEN @has_stock_reference_id = 1 AND @has_stock_lot_id = 1
        THEN 'ALTER TABLE stock DROP COLUMN reference_id'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- STOCK_MOVEMENT: reference_id -> lot_id (legacy compatibility only)
SET @has_movement_reference_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock_movement'
      AND COLUMN_NAME = 'reference_id'
);
SET @has_movement_lot_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock_movement'
      AND COLUMN_NAME = 'lot_id'
);

SET @sql := CASE
    WHEN @has_movement_reference_id = 1 AND @has_movement_lot_id = 0
        THEN 'ALTER TABLE stock_movement RENAME COLUMN reference_id TO lot_id'
    WHEN @has_movement_reference_id = 1 AND @has_movement_lot_id = 1
        THEN 'UPDATE stock_movement SET lot_id = COALESCE(lot_id, reference_id) WHERE reference_id IS NOT NULL'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := CASE
    WHEN @has_movement_reference_id = 1 AND @has_movement_lot_id = 1
        THEN 'ALTER TABLE stock_movement DROP COLUMN reference_id'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
