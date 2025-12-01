package gestion.securelife.repository;


import gestion.securelife.entity.ContratAssuranceHabitation;
import gestion.securelife.entity.enums.ContratStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratAssuranceHabitationRepository extends JpaRepository<ContratAssuranceHabitation, String> {

    boolean existsByEmailAndStatus(String email, ContratStatus status);
    // Récupère tous les contrats d’un client spécifique (email) avec pagination
    List<ContratAssuranceHabitation> findByEmail(String email, Pageable pageable);

}
