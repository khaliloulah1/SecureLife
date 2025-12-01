package gestion.securelife.service;

import gestion.securelife.dto.request.ContratAutoRequest;
import gestion.securelife.dto.response.ContratAutoResponse;
import gestion.securelife.entity.ContratAssuranceAuto;
import gestion.securelife.entity.User;
import gestion.securelife.entity.enums.ContratStatus;
import gestion.securelife.entity.enums.Role;
import gestion.securelife.exception.DuplicateResourceException;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.mapper.ContratMapper;
import gestion.securelife.repository.ContratAssuranceAutoRepository;
import gestion.securelife.repository.UserRepository;
import gestion.securelife.service.interf.ContratAutoService;
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
public class ContratAutoServiceImpl implements ContratAutoService {

    private final ContratAssuranceAutoRepository repository;
    private final ContratMapper mapper;
    private final EmailService emailService;
    private final UserRepository userRepository;

    // Récupérer l'utilisateur connecté
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (User) auth.getPrincipal();
    }

    // Log générique
    private void logAction(String action, String contratId) {
        User user = getCurrentUser();
        String userInfo = (user != null) ? user.getNomComplet() + " [" + user.getRole() + "]" : "ANONYME";
        log.info("{} sur contrat {} par {}", action, contratId, userInfo);
    }

    // Création contrat
    @Override
    @Transactional
    @CacheEvict(value = {"contract-search", "stats"}, allEntries = true)
    public ContratAutoResponse createContrat(ContratAutoRequest request) {

        User currentUser = getCurrentUser();
        log.info("Utilisateur {} crée un contrat auto pour {}",
                currentUser != null ? currentUser.getNomComplet() : "ANONYME",
                request.getNom_complet());

        // Vérification email et immatriculation uniques
        if (repository.existsByEmailAndStatus(request.getEmail(), ContratStatus.ACTIVE))
            throw new DuplicateResourceException("Email déjà utilisé pour un contrat actif");
        if (repository.existsByImmatriculation(request.getImmatriculation()))
            throw new DuplicateResourceException("Immatriculation déjà assurée");

        // Mapping DTO -> Entity
        ContratAssuranceAuto contrat = mapper.toAuto(request);
        contrat.setPrimeAnnuelle(
                CalculPrime.auto(
                        contrat.getPrimeBase(),
                        contrat.getBonusMalus(),
                        contrat.getPuissanceFiscale()
                )
        );
        contrat.setNumeroContrat(GenererNumeroContrat.generate());

        // Attribution du client
        User contratClient;
        if (request.getClient_id() != null && currentUser.getRole() == Role.ADMIN) {
            contratClient = userRepository.findById(request.getClient_id())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        } else {
            contratClient = currentUser; // CLIENT ou AGENT
        }
        contrat.setClient(contratClient);

        // Sauvegarde
        ContratAssuranceAuto saved = repository.save(contrat);
        logAction("Création", String.valueOf(saved.getId()));

        // Retour DTO + Email
        ContratAutoResponse response = mapper.toAutoResponse(saved);
        emailService.sendContractCreatedEmail(response);
        return response;
    }

    // Récupérer contrat par ID
    @Override
    @Cacheable(value = "contract-by-id", key = "#id")
    public ContratAutoResponse getContratById(String id) {
        ContratAssuranceAuto contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.CLIENT
                && !contrat.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé à ce contrat");
        }

        logAction("Consultation", id);
        return mapper.toAutoResponse(contrat);
    }

    // Mise à jour contrat
    @Override
    @Transactional
    @CacheEvict(value = "contract-by-id", key = "#id")
    public ContratAutoResponse updateContrat(String id, ContratAutoRequest request) {
        ContratAssuranceAuto contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        mapper.updateAutoFromRequest(contrat, request);
        contrat.setPrimeAnnuelle(
                CalculPrime.auto(
                        contrat.getPrimeBase(),
                        contrat.getBonusMalus(),
                        contrat.getPuissanceFiscale()
                )
        );
        ContratAssuranceAuto saved = repository.save(contrat);
        logAction("Modification", String.valueOf(saved.getId()));

        ContratAutoResponse response = mapper.toAutoResponse(saved);
        emailService.sendContractUpdatedEmail(response);
        return response;
    }

    // Suppression contrat
    @Override
    @Transactional
    @CacheEvict(value = {"contract-by-id", "contract-search", "stats"}, allEntries = true)
    public void deleteContrat(String id) {
        User currentUser = getCurrentUser();
        if (currentUser != null && (currentUser.getRole() == Role.CLIENT || currentUser.getRole() == Role.AGENT)) {
            throw new AccessDeniedException("Seuls les ADMIN peuvent supprimer un contrat");
        }

        ContratAssuranceAuto contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));
        repository.delete(contrat);
        logAction("Suppression", id);
    }

    // Liste paginée
    @Override
    public List<ContratAutoResponse> getAllContrats(int page, int size) {
        User currentUser = getCurrentUser();

        List<ContratAssuranceAuto> contrats;
        if (currentUser != null && currentUser.getRole() == Role.CLIENT) {
            contrats = repository.findByClientId(currentUser.getId(), PageRequest.of(page, size));
        } else {
            contrats = repository.findAll(PageRequest.of(page, size)).stream().toList();
        }

        log.info("Utilisateur {} récupère {} contrats",
                currentUser != null ? currentUser.getNomComplet() + " [" + currentUser.getRole() + "]" : "ANONYME",
                contrats.size());

        return contrats.stream()
                .map(mapper::toAutoResponse)
                .collect(Collectors.toList());
    }
}
