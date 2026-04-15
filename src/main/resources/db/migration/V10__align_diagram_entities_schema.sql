-- Align schema with backend models for diagram entities only.
-- Scope: huilerie, matiere_premiere, campagne_olives, lot_olives, machine,
-- guide_production, etape_production, parametre_etape, execution_production,
-- valeur_reelle_parametre, produit_final, stock, stock_movement, analyse_laboratoire.

CREATE TABLE IF NOT EXISTS campagne_olives (
    id_campagne BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    annee VARCHAR(255) NOT NULL,
    date_debut VARCHAR(255),
    date_fin VARCHAR(255),
    huilerie_id BIGINT,
    CONSTRAINT uk_campagne_annee UNIQUE (annee),
    CONSTRAINT uk_campagne_reference UNIQUE (reference),
    CONSTRAINT fk_campagne_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie)
);

CREATE TABLE IF NOT EXISTS matiere_premiere (
    id_matiere_premiere BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    nom VARCHAR(255),
    type VARCHAR(255),
    unite_mesure VARCHAR(255),
    description VARCHAR(1000),
    huilerie_id BIGINT NOT NULL,
    CONSTRAINT uk_matiere_reference UNIQUE (reference),
    CONSTRAINT fk_matiere_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie)
);

CREATE TABLE IF NOT EXISTS lot_olives (
    id_lot BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    variete VARCHAR(255),
    maturite VARCHAR(255),
    origine VARCHAR(255),
    date_recolte VARCHAR(255),
    date_reception VARCHAR(255),
    fournisseur_nom VARCHAR(255),
    fournisseur_cin VARCHAR(255),
    duree_stockage_avant_broyage INT,
    pesee DOUBLE,
    quantite_initiale DOUBLE,
    quantite_restante DOUBLE,
    matiere_premiere_id BIGINT NOT NULL,
    campagne_id BIGINT NOT NULL,
    huilerie_id BIGINT,
    CONSTRAINT uk_lot_reference UNIQUE (reference),
    CONSTRAINT fk_lot_matiere FOREIGN KEY (matiere_premiere_id) REFERENCES matiere_premiere (id_matiere_premiere),
    CONSTRAINT fk_lot_campagne FOREIGN KEY (campagne_id) REFERENCES campagne_olives (id_campagne),
    CONSTRAINT fk_lot_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie)
);

CREATE TABLE IF NOT EXISTS machine (
    id_machine BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    nom_machine VARCHAR(255),
    type_machine VARCHAR(255),
    etat_machine VARCHAR(255),
    capacite INT,
    huilerie_id BIGINT,
    matiere_premiere_id BIGINT,
    CONSTRAINT uk_machine_reference UNIQUE (reference),
    CONSTRAINT fk_machine_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie),
    CONSTRAINT fk_machine_matiere FOREIGN KEY (matiere_premiere_id) REFERENCES matiere_premiere (id_matiere_premiere)
);

CREATE TABLE IF NOT EXISTS guide_production (
    id_guide_production BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    date_creation VARCHAR(255),
    huilerie_id BIGINT NOT NULL,
    CONSTRAINT uk_guide_reference UNIQUE (reference),
    CONSTRAINT fk_guide_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie)
);

CREATE TABLE IF NOT EXISTS etape_production (
    id_etape_production BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    ordre INT NOT NULL,
    description VARCHAR(1000),
    guide_production_id BIGINT NOT NULL,
    CONSTRAINT fk_etape_guide FOREIGN KEY (guide_production_id) REFERENCES guide_production (id_guide_production)
);

CREATE TABLE IF NOT EXISTS parametre_etape (
    id_parametre_etape BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    unite_mesure VARCHAR(255),
    description VARCHAR(1000),
    valeur VARCHAR(255),
    etape_production_id BIGINT NOT NULL,
    CONSTRAINT fk_parametre_etape FOREIGN KEY (etape_production_id) REFERENCES etape_production (id_etape_production)
);

CREATE TABLE IF NOT EXISTS execution_production (
    id_execution_production BIGINT AUTO_INCREMENT PRIMARY KEY,
    code_lot VARCHAR(255) NOT NULL,
    date_debut VARCHAR(255),
    date_fin_prevue VARCHAR(255),
    date_fin_reelle VARCHAR(255),
    statut VARCHAR(255) NOT NULL,
    rendement DOUBLE,
    observations VARCHAR(1000),
    guide_production_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    lot_olives_id BIGINT NOT NULL,
    matiere_premiere_id BIGINT NOT NULL,
    CONSTRAINT uk_execution_code_lot UNIQUE (code_lot),
    CONSTRAINT fk_execution_guide FOREIGN KEY (guide_production_id) REFERENCES guide_production (id_guide_production),
    CONSTRAINT fk_execution_machine FOREIGN KEY (machine_id) REFERENCES machine (id_machine),
    CONSTRAINT fk_execution_lot FOREIGN KEY (lot_olives_id) REFERENCES lot_olives (id_lot),
    CONSTRAINT fk_execution_matiere FOREIGN KEY (matiere_premiere_id) REFERENCES matiere_premiere (id_matiere_premiere)
);

CREATE TABLE IF NOT EXISTS produit_final (
    id_produit BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    nom_produit VARCHAR(255),
    quantite_produite DOUBLE,
    date_production VARCHAR(255),
    production_id BIGINT NOT NULL,
    CONSTRAINT uk_produit_reference UNIQUE (reference),
    CONSTRAINT uk_produit_production UNIQUE (production_id),
    CONSTRAINT fk_produit_execution FOREIGN KEY (production_id) REFERENCES execution_production (id_execution_production)
);

CREATE TABLE IF NOT EXISTS stock (
    id_stock BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    type_stock VARCHAR(255),
    quantite_disponible DOUBLE,
    huilerie_id BIGINT NOT NULL,
    lot_id BIGINT,
    produit_id BIGINT,
    CONSTRAINT fk_stock_huilerie FOREIGN KEY (huilerie_id) REFERENCES huilerie (id_huilerie),
    CONSTRAINT fk_stock_lot FOREIGN KEY (lot_id) REFERENCES lot_olives (id_lot),
    CONSTRAINT fk_stock_produit FOREIGN KEY (produit_id) REFERENCES produit_final (id_produit)
);

CREATE TABLE IF NOT EXISTS stock_movement (
    id_stock_movement BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    commentaire VARCHAR(1000),
    date_mouvement VARCHAR(255),
    type_mouvement VARCHAR(50),
    stock_id BIGINT NOT NULL,
    CONSTRAINT fk_mouvement_stock FOREIGN KEY (stock_id) REFERENCES stock (id_stock)
);

CREATE TABLE IF NOT EXISTS analyse_laboratoire (
    id_analyse BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(255),
    acidite DOUBLE,
    indice_peroxyde DOUBLE,
    k232 DOUBLE,
    k270 DOUBLE,
    classe_qualite_finale VARCHAR(255),
    date_analyse VARCHAR(255),
    lot_id BIGINT NOT NULL,
    CONSTRAINT uk_analyse_lot UNIQUE (lot_id),
    CONSTRAINT fk_analyse_lot FOREIGN KEY (lot_id) REFERENCES lot_olives (id_lot)
);

CREATE TABLE IF NOT EXISTS valeur_reelle_parametre (
    id_valeur_reelle_parametre BIGINT AUTO_INCREMENT PRIMARY KEY,
    valeur_reelle VARCHAR(255) NOT NULL,
    parametre_etape_id BIGINT NOT NULL,
    execution_production_id BIGINT NOT NULL,
    CONSTRAINT fk_valeur_parametre FOREIGN KEY (parametre_etape_id) REFERENCES parametre_etape (id_parametre_etape),
    CONSTRAINT fk_valeur_execution FOREIGN KEY (execution_production_id) REFERENCES execution_production (id_execution_production)
);

-- Align existing tables/columns with current backend field names.
ALTER TABLE campagne_olives
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_debut VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_fin VARCHAR(255),
    ADD COLUMN IF NOT EXISTS huilerie_id BIGINT;

ALTER TABLE matiere_premiere
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS type VARCHAR(255),
    ADD COLUMN IF NOT EXISTS unite_mesure VARCHAR(255),
    ADD COLUMN IF NOT EXISTS description VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS huilerie_id BIGINT;

ALTER TABLE lot_olives
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fournisseur_nom VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fournisseur_cin VARCHAR(255),
    ADD COLUMN IF NOT EXISTS duree_stockage_avant_broyage INT,
    ADD COLUMN IF NOT EXISTS pesee DOUBLE,
    ADD COLUMN IF NOT EXISTS quantite_initiale DOUBLE,
    ADD COLUMN IF NOT EXISTS quantite_restante DOUBLE,
    ADD COLUMN IF NOT EXISTS matiere_premiere_id BIGINT,
    ADD COLUMN IF NOT EXISTS campagne_id BIGINT,
    ADD COLUMN IF NOT EXISTS huilerie_id BIGINT;

ALTER TABLE machine
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS nom_machine VARCHAR(255),
    ADD COLUMN IF NOT EXISTS type_machine VARCHAR(255),
    ADD COLUMN IF NOT EXISTS etat_machine VARCHAR(255),
    ADD COLUMN IF NOT EXISTS capacite INT,
    ADD COLUMN IF NOT EXISTS huilerie_id BIGINT,
    ADD COLUMN IF NOT EXISTS matiere_premiere_id BIGINT;

ALTER TABLE guide_production
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_creation VARCHAR(255),
    ADD COLUMN IF NOT EXISTS huilerie_id BIGINT;

ALTER TABLE execution_production
    ADD COLUMN IF NOT EXISTS code_lot VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_debut VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_fin_prevue VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_fin_reelle VARCHAR(255),
    ADD COLUMN IF NOT EXISTS guide_production_id BIGINT,
    ADD COLUMN IF NOT EXISTS machine_id BIGINT,
    ADD COLUMN IF NOT EXISTS lot_olives_id BIGINT,
    ADD COLUMN IF NOT EXISTS matiere_premiere_id BIGINT;

ALTER TABLE produit_final
    ADD COLUMN IF NOT EXISTS production_id BIGINT,
    ADD COLUMN IF NOT EXISTS nom_produit VARCHAR(255),
    ADD COLUMN IF NOT EXISTS quantite_produite DOUBLE,
    ADD COLUMN IF NOT EXISTS date_production VARCHAR(255);

ALTER TABLE stock
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS type_stock VARCHAR(255),
    ADD COLUMN IF NOT EXISTS quantite_disponible DOUBLE,
    ADD COLUMN IF NOT EXISTS lot_id BIGINT,
    ADD COLUMN IF NOT EXISTS produit_id BIGINT;

ALTER TABLE stock_movement
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS commentaire VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS date_mouvement VARCHAR(255),
    ADD COLUMN IF NOT EXISTS type_mouvement VARCHAR(50),
    DROP COLUMN IF EXISTS quantite;

ALTER TABLE analyse_laboratoire
    ADD COLUMN IF NOT EXISTS reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS lot_id BIGINT;

ALTER TABLE parametre_etape
    ADD COLUMN IF NOT EXISTS unite_mesure VARCHAR(255),
    ADD COLUMN IF NOT EXISTS valeur VARCHAR(255);

ALTER TABLE valeur_reelle_parametre
    ADD COLUMN IF NOT EXISTS execution_production_id BIGINT,
    ADD COLUMN IF NOT EXISTS parametre_etape_id BIGINT;

-- Helpful indexes for relations used by backend queries.
CREATE INDEX IF NOT EXISTS idx_campagne_huilerie_id ON campagne_olives (huilerie_id);
CREATE INDEX IF NOT EXISTS idx_matiere_huilerie_id ON matiere_premiere (huilerie_id);
CREATE INDEX IF NOT EXISTS idx_lot_huilerie_id ON lot_olives (huilerie_id);
CREATE INDEX IF NOT EXISTS idx_lot_matiere_id ON lot_olives (matiere_premiere_id);
CREATE INDEX IF NOT EXISTS idx_lot_campagne_id ON lot_olives (campagne_id);
CREATE INDEX IF NOT EXISTS idx_machine_huilerie_id ON machine (huilerie_id);
CREATE INDEX IF NOT EXISTS idx_machine_matiere_id ON machine (matiere_premiere_id);
CREATE INDEX IF NOT EXISTS idx_stock_huilerie_id ON stock (huilerie_id);
CREATE INDEX IF NOT EXISTS idx_stock_lot_id ON stock (lot_id);
CREATE INDEX IF NOT EXISTS idx_stock_produit_id ON stock (produit_id);
CREATE INDEX IF NOT EXISTS idx_stock_movement_stock_id ON stock_movement (stock_id);
CREATE INDEX IF NOT EXISTS idx_analyse_lot_id ON analyse_laboratoire (lot_id);
CREATE INDEX IF NOT EXISTS idx_execution_guide_id ON execution_production (guide_production_id);
CREATE INDEX IF NOT EXISTS idx_execution_machine_id ON execution_production (machine_id);
CREATE INDEX IF NOT EXISTS idx_execution_lot_id ON execution_production (lot_olives_id);
CREATE INDEX IF NOT EXISTS idx_execution_matiere_id ON execution_production (matiere_premiere_id);
CREATE INDEX IF NOT EXISTS idx_parametre_etape_id ON parametre_etape (etape_production_id);
CREATE INDEX IF NOT EXISTS idx_valeur_parametre_id ON valeur_reelle_parametre (parametre_etape_id);
CREATE INDEX IF NOT EXISTS idx_valeur_execution_id ON valeur_reelle_parametre (execution_production_id);
