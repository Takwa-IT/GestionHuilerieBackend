-- V27: drop legacy fournisseur columns from lot_olives
-- Supplier identity is now handled by fournisseur_id relation.

ALTER TABLE lot_olives
    DROP COLUMN IF EXISTS fournisseur_nom,
    DROP COLUMN IF EXISTS fournisseur_cin;
