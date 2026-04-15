-- Fix stock_movement.type_mouvement to match current backend enum values.
-- Backend expects: ENTREE, TRANSFERT, AJUSTEMENT.

-- 1) Make column flexible to avoid MySQL ENUM truncation errors.
ALTER TABLE stock_movement
    MODIFY COLUMN type_mouvement VARCHAR(50) NULL;

-- 2) Migrate historical values (old English enums) to the current French values.
UPDATE stock_movement
SET type_mouvement = 'ENTREE'
WHERE UPPER(type_mouvement) = 'ARRIVAL';

UPDATE stock_movement
SET type_mouvement = 'TRANSFERT'
WHERE UPPER(type_mouvement) IN ('TRANSFER', 'DEPARTURE');

UPDATE stock_movement
SET type_mouvement = 'AJUSTEMENT'
WHERE UPPER(type_mouvement) = 'ADJUSTMENT';
