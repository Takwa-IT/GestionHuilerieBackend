-- Add missing RBAC module/permissions for Campagne Olives.

INSERT INTO `module` (nom)
SELECT 'CAMPAGNE_OLIVES'
WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'CAMPAGNE_OLIVES');

-- Give ADMIN full rights on CAMPAGNE_OLIVES.
INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 1, 1, 'ADMIN CAMPAGNE OLIVES', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'ADMIN'
  AND m.nom = 'CAMPAGNE_OLIVES'
  AND NOT EXISTS (
      SELECT 1
      FROM permission perm
      WHERE perm.profil_id = p.id_profil
        AND perm.module_id = m.id_module
  );

-- Give RESPONSABLE_PRODUCTION rights used by the new controller.
INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 0, 'RESPONSABLE CAMPAGNE OLIVES', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION'
  AND m.nom = 'CAMPAGNE_OLIVES'
  AND NOT EXISTS (
      SELECT 1
      FROM permission perm
      WHERE perm.profil_id = p.id_profil
        AND perm.module_id = m.id_module
  );
