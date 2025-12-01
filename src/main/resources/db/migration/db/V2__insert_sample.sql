-- 🔹 Insertion de contrats standards (table mère contrats)

-- Contrats Auto
INSERT INTO contrats (numero_contrat, nom_complet, email, prime_base, prime_annuelle, status)
VALUES
    ('AUTO-0001', 'Ndiaye Moussa', 'moussa.ndiaye@example.com', 120000, 150000, 'ACTIVE'),
    ('AUTO-0002', 'Fall Aminata', 'aminata.fall@example.com', 100000, 130000, 'ACTIVE');

-- Contrats Habitation
INSERT INTO contrats (numero_contrat, nom_complet, email, prime_base, prime_annuelle, status)
VALUES
    ('HAB-0001', 'Diop Fatou', 'fatou.diop@example.com', 180000, 200000, 'ACTIVE'),
    ('HAB-0002', 'Gueye Ousmane', 'ousmane.gueye@example.com', 150000, 170000, 'ACTIVE');

-- Contrats Vie
INSERT INTO contrats (numero_contrat, nom_complet, email, prime_base, prime_annuelle, status)
VALUES
    ('VIE-0001', 'Ba Adama', 'adama.ba@example.com', 200000, 250000, 'ACTIVE'),
    ('VIE-0002', 'Sow Mariama', 'mariama.sow@example.com', 220000, 270000, 'ACTIVE');


-- 🔹 Insertion des données dans les tables enfants
-- ⚠ IMPORTANT : On utilise les ID générés automatiquement dans l’ordre d’insertion

-- contrats_assurance_auto
INSERT INTO contrats_assurance_auto (id, immatriculation, puissance_fiscale, bonus_malus)
VALUES
    (1, 'DK-1234-A', 7, 100),
    (2, 'DK-5678-B', 10, 120);

-- contrats_assurance_habitation
INSERT INTO contrats_assurance_habitation (id, adresse, superficie, zone_risque)
VALUES
    (3, 'Dakar, Médina', 85, 'MOYEN'),
    (4, 'Thiès, Grand Standing', 120, 'FAIBLE');

-- contrats_assurance_vie
INSERT INTO contrats_assurance_vie (id, age_assure, capital_garanti, beneficiaire)
VALUES
    (5, 35, 5000000, 'Aïssatou Ba'),
    (6, 50, 8000000, 'Serigne Sow');
