INSERT INTO profil (nom, description, date_creation)
SELECT 'ADMIN', 'Acces total', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM profil WHERE nom = 'ADMIN');

INSERT INTO profil (nom, description, date_creation)
SELECT 'RESPONSABLE_PRODUCTION', 'Acces operations metier', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM profil WHERE nom = 'RESPONSABLE_PRODUCTION');

INSERT INTO `module` (nom)
SELECT 'DASHBOARD' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'DASHBOARD');
INSERT INTO `module` (nom)
SELECT 'RECEPTION' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'RECEPTION');
INSERT INTO `module` (nom)
SELECT 'CAMPAGNE_OLIVES' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'CAMPAGNE_OLIVES');
INSERT INTO `module` (nom)
SELECT 'GUIDE_PRODUCTION' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'GUIDE_PRODUCTION');
INSERT INTO `module` (nom)
SELECT 'MACHINES' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'MACHINES');
INSERT INTO `module` (nom)
SELECT 'MATIERES_PREMIERES' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'MATIERES_PREMIERES');
INSERT INTO `module` (nom)
SELECT 'STOCK' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'STOCK');
INSERT INTO `module` (nom)
SELECT 'STOCK_MOUVEMENT' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'STOCK_MOUVEMENT');
INSERT INTO `module` (nom)
SELECT 'LOTS_TRAÇABILITE' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'LOTS_TRAÇABILITE');
INSERT INTO `module` (nom)
SELECT 'DASHBOARD_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'DASHBOARD_ADMIN');
INSERT INTO `module` (nom)
SELECT 'HUILERIES' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'HUILERIES');
INSERT INTO `module` (nom)
SELECT 'COMPTES_PROFILS' WHERE NOT EXISTS (SELECT 1 FROM `module` WHERE nom = 'COMPTES_PROFILS');

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 1, 1, 'ADMIN ALL', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module
);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 0, 1, 0, 0, 0, 'RESPONSABLE DASHBOARD', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'DASHBOARD'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 0, 0, 1, 'RESPONSABLE RECEPTION', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'RECEPTION'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 0, 'RESPONSABLE CAMPAGNE OLIVES', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'CAMPAGNE_OLIVES'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 1, 'RESPONSABLE GUIDE', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'GUIDE_PRODUCTION'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 0, 1, 0, 0, 0, 'RESPONSABLE MACHINES', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'MACHINES'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 0, 'RESPONSABLE MATIERES', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'MATIERES_PREMIERES'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 0, 'RESPONSABLE STOCK', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'STOCK'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 0, 'RESPONSABLE STOCK MOUVEMENT', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'STOCK_MOUVEMENT'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO permission (can_create, can_read, can_update, can_delete, can_executed, description, date_creation, profil_id, module_id)
SELECT 1, 1, 1, 0, 0, 'RESPONSABLE LOTS', CURRENT_TIMESTAMP, p.id_profil, m.id_module
FROM profil p, `module` m
WHERE p.nom = 'RESPONSABLE_PRODUCTION' AND m.nom = 'LOTS_TRAÇABILITE'
AND NOT EXISTS (SELECT 1 FROM permission perm WHERE perm.profil_id = p.id_profil AND perm.module_id = m.id_module);

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, actif, profil_id, huilerie_id)
SELECT 'Admin', 'Système', 'admin@huilerie.tn', '$2a$10$REPLACE_WITH_BCRYPT_HASH', NULL, 'ACTIF', p.id_profil, h.id_huilerie
FROM profil p, huilerie h
WHERE p.nom = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM utilisateur u WHERE u.email = 'admin@huilerie.tn');