ALTER TABLE utilisateur
    ADD COLUMN entreprise_id BIGINT NULL;

UPDATE utilisateur u
JOIN huilerie h ON h.id_huilerie = u.huilerie_id
SET u.entreprise_id = h.entreprise_id
WHERE u.entreprise_id IS NULL;

ALTER TABLE utilisateur
    MODIFY COLUMN entreprise_id BIGINT NOT NULL;

ALTER TABLE utilisateur
    ADD CONSTRAINT fk_utilisateur_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprise (id_entreprise);

CREATE INDEX idx_utilisateur_entreprise_id ON utilisateur (entreprise_id);
