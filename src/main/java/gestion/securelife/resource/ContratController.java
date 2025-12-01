package gestion.securelife.resource;

import gestion.securelife.dto.filter.ContratFilterRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratResponse;
import gestion.securelife.service.ContratRechercheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/insurances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contrats", description = "Gestion des contrats d'assurance")
public class ContratController {

    private final ContratRechercheService rechercheService;

    @Operation(summary = "Recherche de contrats avec filtres")
    @GetMapping("/search")
    public ResponseEntity<Page<ContratResponse>> searchContrats(
            @RequestParam(required = false) String clientNom_complet,
            @RequestParam(required = false) String clientEmail,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double primeMin,
            @RequestParam(required = false) Double primeMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Recherche de contrats avec filtres: nom={}, email={}, type={}, status={}, primeMin={}, primeMax={}, page={}, size={}",
                clientNom_complet, clientEmail, type, status, primeMin, primeMax, page, size);

        ContratFilterRequest filter = new ContratFilterRequest();
        filter.setClientNom_complet(clientNom_complet);
        filter.setClientEmail(clientEmail);
        filter.setType(type);
        if (status != null) filter.setStatus(Enum.valueOf(gestion.securelife.entity.enums.ContratStatus.class, status));
        filter.setPrimeMin(primeMin);
        filter.setPrimeMax(primeMax);

        List<ContratResponse> result = rechercheService.search(filter);
        log.info("Nombre de contrats trouvés: {}", result.size());

        Page<ContratResponse> paged = new PageImpl<>(result, PageRequest.of(page, size), result.size());
        return ResponseEntity.ok(paged);
    }

    @Operation(summary = "Lister tous les contrats paginés")
    @GetMapping
    public ResponseEntity<Page<ContratResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération de tous les contrats, page={}, size={}", page, size);
        List<ContratResponse> list = rechercheService.getAllContrats(page, size);
        log.info("Nombre de contrats récupérés: {}", list.size());
        Page<ContratResponse> paged = new PageImpl<>(list, PageRequest.of(page, size), list.size());
        return ResponseEntity.ok(paged);
    }

    @Operation(summary = "Récupérer un contrat par ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContratResponse> getById(@PathVariable String id) {
        log.info("Récupération du contrat ID={}", id);
        ContratResponse response = rechercheService.getById(id);
        log.info("Contrat récupéré: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Changer le statut d'un contrat")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContratResponse> updateStatus(
            @PathVariable String id,
            @RequestBody ContratStatusUpdateRequest request
    ) {
        log.info("Mise à jour du statut du contrat ID={} à {}", id, request.getStatus());
        ContratResponse response = rechercheService.updateStatus(id, request);
        log.info("Statut du contrat ID={} mis à jour avec succès", id);
        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Supprimer un contrat")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Suppression du contrat ID={}", id);
        rechercheService.deleteById(id);
        log.info("Contrat ID={} supprimé", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Statistiques des contrats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = rechercheService.getStats();
        log.info("Statistiques récupérées");
        return ResponseEntity.ok(stats);
    }
}
