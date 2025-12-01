-- ===============================================
-- Users et rôles pour MODULE 3 : SÉCURITÉ JWT
-- ===============================================

-- Table des rôles (enum Role dans le code)
-- On stocke les rôles directement dans l'entité User en tant que VARCHAR
-- Donc pas de table séparée Role nécessaire

-- Table Users
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       nom_complet VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL
);

-- Table Contrats (déjà existante, mais on s'assure que client_id référence User)
-- Si tu as déjà la table contrats, adapte juste la foreign key
ALTER TABLE contrats
    ADD COLUMN client_id INT;

ALTER TABLE contrats
    ADD CONSTRAINT fk_client
        FOREIGN KEY (client_id) REFERENCES users(id);

-- Optionnel : index pour accélérer les recherches par email
CREATE UNIQUE INDEX idx_users_email ON users(email);

-- Optionnel : index pour les contrats par client
CREATE INDEX idx_contrats_client ON contrats(client_id);
