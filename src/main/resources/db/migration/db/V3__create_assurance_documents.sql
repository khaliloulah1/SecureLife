-- Création de la table assurance_documents

CREATE TABLE assurance_documents (
                                     id BIGSERIAL PRIMARY KEY,

                                     file_name VARCHAR(255) NOT NULL,
                                     file_path TEXT NOT NULL,
                                     file_type VARCHAR(100) NOT NULL,
                                     file_size BIGINT NOT NULL,

                                     document_type VARCHAR(50) NOT NULL,

                                     uploaded_at TIMESTAMP NOT NULL,

                                     contrat_id INTEGER NOT NULL,

                                     CONSTRAINT fk_document_contrat
                                         FOREIGN KEY (contrat_id)
                                             REFERENCES contrats(id)
                                             ON DELETE CASCADE
);

-- Optionnel : Index pour accélérer les recherches par contrat
CREATE INDEX idx_document_contrat_id
    ON assurance_documents (contrat_id);
