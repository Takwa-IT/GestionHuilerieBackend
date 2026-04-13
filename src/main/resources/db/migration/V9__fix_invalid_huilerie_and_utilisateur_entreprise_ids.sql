INSERT INTO entreprise (nom, adresse, telephone, email)
SELECT 'Entreprise par defaut', NULL, NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM entreprise);

UPDATE huilerie h
JOIN (
    SELECT MIN(id_entreprise) AS fallback_entreprise_id
    FROM entreprise
) e
SET h.entreprise_id = e.fallback_entreprise_id
WHERE h.entreprise_id IS NULL
   OR h.entreprise_id = 0
   OR NOT EXISTS (
       SELECT 1
       FROM entreprise ent
       WHERE ent.id_entreprise = h.entreprise_id
   );

UPDATE utilisateur u
JOIN huilerie h ON h.id_huilerie = u.huilerie_id
SET u.entreprise_id = h.entreprise_id
WHERE u.entreprise_id IS NULL
   OR u.entreprise_id = 0
   OR NOT EXISTS (
       SELECT 1
       FROM entreprise ent
       WHERE ent.id_entreprise = u.entreprise_id
   );
