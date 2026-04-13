UPDATE utilisateur u
JOIN huilerie h ON h.id_huilerie = u.huilerie_id
SET u.entreprise_id = h.entreprise_id
WHERE u.entreprise_id IS NULL
   OR u.entreprise_id = 0;
