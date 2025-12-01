-- Table principale des contrats
CREATE TABLE contrats (
                          id SERIAL PRIMARY KEY,
                          numero_contrat VARCHAR(50) UNIQUE NOT NULL,
                          nom_complet VARCHAR(150) NOT NULL,
                          email VARCHAR(150) NOT NULL,
                          prime_base DOUBLE PRECISION NOT NULL CHECK (prime_base > 0),
                          prime_annuelle DOUBLE PRECISION NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Unicité Email si status = ACTIVE
CREATE UNIQUE INDEX uniq_email_active
    ON contrats(email)
    WHERE status = 'ACTIVE';

-- Table contrats_assurance_auto
CREATE TABLE contrats_assurance_auto (
                                         id SERIAL PRIMARY KEY REFERENCES contrats(id) ON DELETE CASCADE,
                                         immatriculation VARCHAR(20) UNIQUE NOT NULL,
                                         puissance_fiscale INT NOT NULL CHECK (puissance_fiscale BETWEEN 1 AND 50),
                                         bonus_malus INT NOT NULL CHECK (bonus_malus BETWEEN 50 AND 350)
);

-- Table contrats_assurance_habitation
CREATE TABLE contrats_assurance_habitation (
                                               id SERIAL PRIMARY KEY REFERENCES contrats(id) ON DELETE CASCADE,
                                               adresse VARCHAR(255) NOT NULL,
                                               superficie DOUBLE PRECISION NOT NULL CHECK (superficie >= 10),
                                               zone_risque VARCHAR(50) NOT NULL
);

-- Table contrats_assurance_vie
CREATE TABLE contrats_assurance_vie (
                                        id SERIAL PRIMARY KEY REFERENCES contrats(id) ON DELETE CASCADE,
                                        age_assure INT NOT NULL CHECK (age_assure BETWEEN 18 AND 80),
                                        capital_garanti DOUBLE PRECISION NOT NULL CHECK (capital_garanti >= 10000),
                                        beneficiaire VARCHAR(150) NOT NULL
);
