-- Remove obsolete direct relations from stock.
-- Stock must only keep its lot association; huilerie is derived through lot_olives.

SET @has_stock_huilerie_fk := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock'
      AND CONSTRAINT_NAME = 'fk_stock_huilerie'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql := CASE
    WHEN @has_stock_huilerie_fk = 1
        THEN 'ALTER TABLE stock DROP FOREIGN KEY fk_stock_huilerie'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_stock_produit_fk := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock'
      AND CONSTRAINT_NAME = 'fk_stock_produit'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql := CASE
    WHEN @has_stock_produit_fk = 1
        THEN 'ALTER TABLE stock DROP FOREIGN KEY fk_stock_produit'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_stock_huilerie_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock'
      AND COLUMN_NAME = 'huilerie_id'
);

SET @sql := CASE
    WHEN @has_stock_huilerie_column = 1
        THEN 'ALTER TABLE stock DROP COLUMN huilerie_id'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_stock_produit_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'stock'
      AND COLUMN_NAME = 'produit_id'
);

SET @sql := CASE
    WHEN @has_stock_produit_column = 1
        THEN 'ALTER TABLE stock DROP COLUMN produit_id'
    ELSE 'SELECT 1'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;