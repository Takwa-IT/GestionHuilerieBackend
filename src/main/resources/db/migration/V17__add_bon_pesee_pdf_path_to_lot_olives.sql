-- Persist generated bon de pesee PDF path for each lot d'olives
ALTER TABLE lot_olives
    ADD COLUMN IF NOT EXISTS bon_pesee_pdf_path VARCHAR(512);
