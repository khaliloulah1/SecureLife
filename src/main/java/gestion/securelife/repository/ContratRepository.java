package gestion.securelife.repository;


import gestion.securelife.entity.Contrat;
import gestion.securelife.entity.enums.ContratStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, String>, JpaSpecificationExecutor<Contrat> {

    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    boolean existsByEmailAndStatus(String email, ContratStatus status);

    // Optionnel : filtrage par client pour requêtes simples

}
