-- Allow etape_production.machine_id to be NULL
-- This permits steps like "reception" that don't require a machine

-- First, check if the constraint exists and drop it
ALTER TABLE etape_production
    DROP FOREIGN KEY IF EXISTS fk_etape_production_machine;

-- Modify the column to allow NULL
ALTER TABLE etape_production
    MODIFY COLUMN machine_id BIGINT NULL;

-- Re-add the foreign key constraint allowing NULL
ALTER TABLE etape_production
    ADD CONSTRAINT fk_etape_production_machine
    FOREIGN KEY (machine_id) REFERENCES machine(id_machine);
