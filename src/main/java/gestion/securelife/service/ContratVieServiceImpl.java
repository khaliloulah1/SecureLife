package gestion.securelife.service;

import gestion.securelife.dto.request.ContratVieRequest;
import gestion.securelife.dto.response.ContratVieResponse;
import gestion.securelife.entity.ContratAssuranceVie;
import gestion.securelife.entity.User;
import gestion.securelife.entity.enums.ContratStatus;
import gestion.securelife.entity.enums.Role;
import gestion.securelife.exception.DuplicateResourceException;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.mapper.ContratMapper;
import gestion.securelife.repository.ContratAssuranceVieRepository;
import gestion.securelife.service.interf.ContratVieService;
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
public class ContratVieServiceImpl implements ContratVieService {

    private final ContratAssuranceVieRepository repository;
    private final ContratMapper mapper;
    private final EmailService emailService;

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
        String userInfo = (user != null)
                ? user.getNomComplet() + " [" + user.getRole() + "]"
                : "ANONYME";

        log.info("{} sur contrat {} par {}", action, contratId, userInfo);
    }

    // ------------------------------
    // Création contrat
    // ------------------------------
    @Override
    @Transactional
    @CacheEvict(value = {"contract-search", "stats"}, allEntries = true)
    public ContratVieResponse createContrat(ContratVieRequest request) {

        User currentUser = getCurrentUser();
        assert currentUser != null;

        if (currentUser.getRole() == Role.CLIENT) {
            throw new AccessDeniedException("Un client ne peut pas créer un contrat");
        }

        log.info("Utilisateur {} crée un contrat-vie pour {}", currentUser.getNomComplet(), request.getEmail());

        if (repository.existsByEmailAndStatus(request.getEmail(), ContratStatus.ACTIVE)) {
            throw new DuplicateResourceException("Un contrat actif existe déjà pour cet email");
        }

        // Mapping DTO -> Entity
        ContratAssuranceVie contrat = mapper.toVie(request);
        contrat.setPrimeAnnuelle(
                CalculPrime.vie(
                        contrat.getPrimeBase(),
                        contrat.getCapitalGaranti(),
                        contrat.getAgeAssure()
                )
        );
        contrat.setNumeroContrat(GenererNumeroContrat.generate("LIFE"));

        // Attribution du client automatiquement à l'utilisateur connecté
        contrat.setUser(currentUser);

        // Sauvegarde
        ContratAssuranceVie saved = repository.save(contrat);
        logAction("Création", String.valueOf(saved.getId()));

        ContratVieResponse response = mapper.toVieResponse(saved);
        emailService.sendContractCreatedEmail(response);

        return response;
    }

    // ------------------------------
    // Récupérer contrat par ID
    // ------------------------------
    @Override
    @Cacheable(value = "contract-by-id", key = "#id")
    public ContratVieResponse getContratById(String id) {
        ContratAssuranceVie contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        User currentUser = getCurrentUser();
        assert currentUser != null;

        // CLIENT → accès uniquement à ses contrats
        if (currentUser.getRole() == Role.CLIENT &&
                !contrat.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé : ce contrat ne vous appartient pas");
        }

        logAction("Consultation", id);
        return mapper.toVieResponse(contrat);
    }

    // ------------------------------
    // Mise à jour contrat
    // ------------------------------
    @Override
    @Transactional
    @CacheEvict(value = {"contract-by-id", "contract-search", "stats"}, allEntries = true)
    public ContratVieResponse updateContrat(String id, ContratVieRequest request) {

        ContratAssuranceVie contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        User currentUser = getCurrentUser();
        assert currentUser != null;

        // CLIENT → peut modifier uniquement ses contrats
        if (currentUser.getRole() == Role.CLIENT &&
                !contrat.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous ne pouvez pas modifier ce contrat");
        }

        mapper.updateVieFromRequest(contrat, request);

        contrat.setPrimeAnnuelle(
                CalculPrime.vie(
                        contrat.getPrimeBase(),
                        contrat.getCapitalGaranti(),
                        contrat.getAgeAssure()
                )
        );

        ContratAssuranceVie saved = repository.save(contrat);
        logAction("Modification", String.valueOf(saved.getId()));

        ContratVieResponse response = mapper.toVieResponse(saved);
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
        assert currentUser != null;

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Seul un ADMIN peut supprimer un contrat");
        }

        ContratAssuranceVie contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        repository.delete(contrat);
        logAction("Suppression", id);
    }

    // ------------------------------
    // Liste paginée des contrats
    // ------------------------------
    @Override
    public List<ContratVieResponse> getAllContrats(int page, int size) {

        User currentUser = getCurrentUser();
        assert currentUser != null;

        List<ContratAssuranceVie> contrats;

        if (currentUser.getRole() == Role.CLIENT) {
            contrats = repository.findByEmail(currentUser.getEmail(), PageRequest.of(page, size));
        } else {
            contrats = repository.findAll(PageRequest.of(page, size)).stream().toList();
        }

        log.info("Utilisateur {} récupère {} contrats-vie",
                currentUser.getNomComplet() + " [" + currentUser.getRole() + "]",
                contrats.size());

        return contrats.stream()
                .map(mapper::toVieResponse)
                .collect(Collectors.toList());
    }
}
