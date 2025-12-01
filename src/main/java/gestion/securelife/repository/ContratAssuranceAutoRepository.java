package gestion.securelife.repository;


import gestion.securelife.entity.ContratAssuranceAuto;
import gestion.securelife.entity.enums.ContratStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ContratAssuranceAutoRepository extends JpaRepository<ContratAssuranceAuto, String> {

    boolean existsByEmailAndStatus(String email, ContratStatus status);
    // Récupère les contrats d’un client par email avec pagination
    List<ContratAssuranceAuto> findByEmail(String email, Pageable pageable);
    boolean existsByImmatriculation(String immatriculation);
    List<ContratAssuranceAuto> findByClientId(Integer clientId, Pageable pageable);

}
