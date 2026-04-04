CREATE TABLE IF NOT EXISTS profil (
    id_profil BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_profil_nom UNIQUE (nom)
);

CREATE TABLE IF NOT EXISTS `module` (
    id_module BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    CONSTRAINT uk_module_nom UNIQUE (nom)
);

CREATE TABLE IF NOT EXISTS permission (
    id_privilege BIGINT AUTO_INCREMENT PRIMARY KEY,
    can_create BIT NOT NULL DEFAULT b'0',
    can_read BIT NOT NULL DEFAULT b'0',
    can_update BIT NOT NULL DEFAULT b'0',
    can_delete BIT NOT NULL DEFAULT b'0',
    can_executed BIT NOT NULL DEFAULT b'0',
    description VARCHAR(1000),
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profil_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    CONSTRAINT fk_permission_profil FOREIGN KEY (profil_id) REFERENCES profil (id_profil),
    CONSTRAINT fk_permission_module FOREIGN KEY (module_id) REFERENCES `module` (id_module),
    CONSTRAINT uk_permission_profil_module UNIQUE (profil_id, module_id)
);

CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    telephone VARCHAR(50),
    actif VARCHAR(20) NOT NULL,
    profil_id BIGINT NOT NULL,
    huilerie_id BIGINT NOT NULL,
    CONSTRAINT fk_utilisateur_profil FOREIGN KEY (profil_id) REFERENCES profil (id_profil),
    CONSTRAINT fk_utilisateur_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie)
);

CREATE INDEX idx_permission_profil_id ON permission (profil_id);
CREATE INDEX idx_permission_module_id ON permission (module_id);
CREATE INDEX idx_utilisateur_id ON utilisateur (id_utilisateur);