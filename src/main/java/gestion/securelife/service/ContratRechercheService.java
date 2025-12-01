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

        User currentUser = getCurrentUser();
        log.info("🔧 [UPDATE STATUS] User={} | Role={} | ContratID={} | NouveauStatus={}",
                currentUser.getEmail(), currentUser.getRole(), id, request.getStatus());

        if (currentUser.getRole() == Role.CLIENT)
            throw new AccessDeniedException("Vous ne pouvez pas changer le statut");

        Contrat contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

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
        log.info("🔍 [SEARCH] User={} | Role={} | Filtres={}", currentUser.getEmail(), currentUser.getRole(), filter);

        Specification<Contrat> spec = Specification
                .where(ContratSpecification.hasClientNom_complet(filter.getClientNom_complet()))
                .and(ContratSpecification.hasEmail(filter.getClientEmail()))
                .and(ContratSpecification.hasType(filter.getType()))
                .and(ContratSpecification.hasStatus(filter.getStatus()))
                .and(ContratSpecification.primeBetween(filter.getPrimeMin(), filter.getPrimeMax()));

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
        log.info("📄 [GET ALL CONTRACTS] User={} | Role={} | Page={} | Size={}", currentUser.getEmail(), currentUser.getRole(), page, size);

        List<Contrat> contrats = repository.findAll(PageRequest.of(page, size)).stream().toList();

        return contrats.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------
    // GET BY ID
    // --------------------------
    @Cacheable(value = "contract-by-id", key = "#id")
    public ContratResponse getById(String id) {

        User currentUser = getCurrentUser();
        log.info("🔍 [GET BY ID] User={} | Role={} | ContratID={}", currentUser.getEmail(), currentUser.getRole(), id);

        Contrat contrat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));

        if (currentUser.getRole() == Role.CLIENT &&
                !contrat.getUser().getId().equals(currentUser.getId())) {
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
        log.info("❌ [DELETE] User={} | Role={} | ContratID={}", currentUser.getEmail(), currentUser.getRole(), id);

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
        log.info("📊 [STATS] User={} | Role={}", currentUser.getEmail(), currentUser.getRole());

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
