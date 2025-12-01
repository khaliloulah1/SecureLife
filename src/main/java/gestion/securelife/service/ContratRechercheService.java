package gestion.securelife.service;

import gestion.securelife.dto.filter.ContratFilterRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratResponse;
import gestion.securelife.entity.Contrat;
import gestion.securelife.entity.User;
import gestion.securelife.entity.enums.ContratStatus;
import gestion.securelife.entity.enums.Role;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.mapper.ContratMapper;
import gestion.securelife.repository.ContratRepository;
import gestion.securelife.repository.specification.ContratSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContratRechercheService {

    private final ContratRepository repository;
    private final ContratMapper mapper;
    private final EmailService emailService;

    // --------------------------
    // USER CONNECTÉ
    // --------------------------
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (User) auth.getPrincipal();
    }

    // --------------------------
    // UPDATE STATUS
    // --------------------------
    @Transactional
    @CacheEvict(value = {"contract-by-id", "contract-search", "stats"}, allEntries = true)
    public ContratResponse updateStatus(String id, ContratStatusUpdateRequest request) {

        Contrat contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CLIENT)
            throw new AccessDeniedException("Vous ne pouvez pas changer le statut");

        contrat.setStatus(request.getStatus());

        Contrat updated = repository.save(contrat);
        emailService.sendContractStatusChangedEmail(mapper.toResponse(updated));

        return mapper.toResponse(updated);
    }

    // --------------------------
    // SEARCH + FILTER
    // --------------------------
    @Cacheable(value = "contract-search", key = "#filter.hashCode()")
    public List<ContratResponse> search(ContratFilterRequest filter) {

        User currentUser = getCurrentUser();

        Specification<Contrat> spec = Specification
                .allOf(ContratSpecification.hasClientNom_complet(filter.getClientNom_complet()))
                .and(ContratSpecification.hasEmail(filter.getClientEmail()))
                .and(ContratSpecification.hasType(filter.getType()))
                .and(ContratSpecification.hasStatus(filter.getStatus()))
                .and(ContratSpecification.primeBetween(filter.getPrimeMin(), filter.getPrimeMax()));

        if (currentUser.getRole() == Role.CLIENT) {
            spec = spec.and(ContratSpecification.hasClientId(currentUser.getId()));
        }

        log.info("DB CALL - Recherche avec filtres {}", filter);

        return repository.findAll(spec)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------
    // GET ALL PAGINÉE
    // --------------------------
    public List<ContratResponse> getAllContrats(int page, int size) {

        User currentUser = getCurrentUser();
        List<Contrat> contrats;

        if (currentUser.getRole() == Role.CLIENT) {
            contrats = repository.findByClientId(currentUser.getId(), PageRequest.of(page, size));
        } else {
            contrats = repository.findAll(PageRequest.of(page, size)).stream().toList();
        }

        return contrats.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------
    // GET BY ID
    // --------------------------
    @Cacheable(value = "contract-by-id", key = "#id")
    public ContratResponse getById(String id) {

        Contrat contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CLIENT &&
                !contrat.getClient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé");
        }

        return mapper.toResponse(contrat);
    }

    // --------------------------
    // DELETE
    // --------------------------
    @Transactional
    @CacheEvict(value = {"contract-by-id", "contract-search", "stats"}, allEntries = true)
    public void deleteById(String id) {

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN)
            throw new AccessDeniedException("Seul ADMIN peut supprimer");

        Contrat contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        repository.delete(contrat);
    }

    // --------------------------
    // STATS
    // --------------------------
    @Cacheable(value = "stats")
    public Map<String, Object> getStats() {

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN)
            throw new AccessDeniedException("Seul ADMIN peut accéder aux stats");

        Map<String, Object> stats = new HashMap<>();

        for (ContratStatus status : ContratStatus.values()) {
            stats.put(status.name(),
                    repository.count((root, query, cb) -> cb.equal(root.get("status"), status)));
        }

        stats.put("chiffreAffairesTotal",
                repository.findAll().stream()
                        .mapToDouble(Contrat::getPrimeAnnuelle)
                        .sum()
        );

        return stats;
    }
}
