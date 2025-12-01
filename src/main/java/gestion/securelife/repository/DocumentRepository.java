package gestion.securelife.repository;

import gestion.securelife.entity.Contrat;
import gestion.securelife.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByContrat(Contrat contrat);

    long countByContrat(Contrat contrat);
}
