package gestion.securelife.service;

import gestion.securelife.dto.request.ContratHabitationRequest;
import gestion.securelife.dto.response.ContratHabitationResponse;
import gestion.securelife.entity.ContratAssuranceHabitation;
import gestion.securelife.entity.User;
import gestion.securelife.entity.enums.ContratStatus;
import gestion.securelife.entity.enums.Role;
import gestion.securelife.exception.DuplicateResourceException;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.mapper.ContratMapper;
import gestion.securelife.repository.ContratAssuranceHabitationRepository;
import gestion.securelife.repository.UserRepository;
import gestion.securelife.service.interf.ContratHabitationService;
import gestion.securelife.util.CalculPrime;
import gestion.securelife.util.GenererNumeroContrat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContratHabitationServiceImpl implements ContratHabitationService {

    private final ContratAssuranceHabitationRepository repository;
    private final ContratMapper mapper;
    private final EmailService emailService;
    private final UserRepository userRepository;

    // ------------------------------
    // Récupérer l'utilisateur connecté
    // ------------------------------
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (User) auth.getPrincipal();
    }

    // ------------------------------
    // Log générique
    // ------------------------------
    private void logAction(String action, String contratId) {
        User user = getCurrentUser();
        String userInfo = (user != null) ? user.getNomComplet() + " [" + user.getRole() + "]" : "ANONYME";
        log.info("{} sur contrat {} par {}", action, contratId, userInfo);
    }

    // ------------------------------
    // Création contrat
    // ------------------------------
    @Override
    @Transactional
    @CacheEvict(value = {"contract-search", "stats"}, allEntries = true)
    public ContratHabitationResponse createContrat(ContratHabitationRequest request) {
        User currentUser = getCurrentUser();
        log.info("Utilisateur {} crée un contrat habitation pour {}",
                currentUser != null ? currentUser.getNomComplet() : "ANONYME",
                request.getNom_complet());

        if (repository.existsByEmailAndStatus(request.getEmail(), ContratStatus.ACTIVE))
            throw new DuplicateResourceException("Email déjà utilisé pour un contrat actif");

        // Mapping DTO -> Entity
        ContratAssuranceHabitation contrat = mapper.toHabitation(request);
        contrat.setPrimeAnnuelle(
                CalculPrime.habitation(
                        contrat.getPrimeBase(),
                        contrat.getSuperficie(),
                        contrat.getZoneRisque()
                )
        );
        contrat.setNumeroContrat(GenererNumeroContrat.generate());

        // Attribution du client
        User contratClient;
        if (request.getClient_id() != null && currentUser.getRole() == Role.ADMIN) {
            contratClient = userRepository.findById(request.getClient_id())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        } else {
            contratClient = currentUser;
        }
        contrat.setClient(contratClient);

        // Sauvegarde
        ContratAssuranceHabitation saved = repository.save(contrat);
        logAction("Création", String.valueOf(saved.getId()));

        // Retour DTO + Email
        ContratHabitationResponse response = mapper.toHabitationResponse(saved);
        emailService.sendContractCreatedEmail(response);
        return response;
    }

    // ------------------------------
    // Récupérer contrat par ID
    // ------------------------------
    @Override
    @Cacheable(value = "contract-by-id", key = "#id")
    public ContratHabitationResponse getContratById(String id) {
        ContratAssuranceHabitation contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.CLIENT
                && !contrat.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé à ce contrat");
        }

        logAction("Consultation", id);
        return mapper.toHabitationResponse(contrat);
    }

    // ------------------------------
    // Mise à jour contrat
    // ------------------------------
    @Override
    @Transactional
    @CacheEvict(value = "contract-by-id", key = "#id")
    public ContratHabitationResponse updateContrat(String id, ContratHabitationRequest request) {
        ContratAssuranceHabitation contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        mapper.updateHabitationFromRequest(contrat, request);
        contrat.setPrimeAnnuelle(
                CalculPrime.habitation(
                        contrat.getPrimeBase(),
                        contrat.getSuperficie(),
                        contrat.getZoneRisque()
                )
        );

        ContratAssuranceHabitation saved = repository.save(contrat);
        logAction("Modification", String.valueOf(saved.getId()));

        ContratHabitationResponse response = mapper.toHabitationResponse(saved);
        emailService.sendContractUpdatedEmail(response);
        return response;
    }

    // ------------------------------
    // Suppression contrat
    // ------------------------------
    @Override
    @Transactional
    @CacheEvict(value = {"contract-by-id", "contract-search", "stats"}, allEntries = true)
    public void deleteContrat(String id) {
        User currentUser = getCurrentUser();
        if (currentUser != null && (currentUser.getRole() == Role.CLIENT || currentUser.getRole() == Role.AGENT)) {
            throw new AccessDeniedException("Seuls les ADMIN peuvent supprimer un contrat");
        }

        ContratAssuranceHabitation contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));
        repository.delete(contrat);
        logAction("Suppression", id);
    }

    // ------------------------------
    // Liste paginée
    // ------------------------------
    @Override
    public List<ContratHabitationResponse> getAllContrats(int page, int size) {
        User currentUser = getCurrentUser();

        List<ContratAssuranceHabitation> contrats;
        if (currentUser != null && currentUser.getRole() == Role.CLIENT) {
            contrats = repository.findByClientId(currentUser.getId(), PageRequest.of(page, size));
        } else {
            contrats = repository.findAll(PageRequest.of(page, size)).stream().toList();
        }

        log.info("Utilisateur {} récupère {} contrats",
                currentUser != null ? currentUser.getNomComplet() + " [" + currentUser.getRole() + "]" : "ANONYME",
                contrats.size());

        return contrats.stream()
                .map(mapper::toHabitationResponse)
                .collect(Collectors.toList());
    }
}
