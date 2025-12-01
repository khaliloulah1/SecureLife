package gestion.securelife.resource;

import gestion.securelife.dto.request.ContratHabitationRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratHabitationResponse;
import gestion.securelife.service.interf.ContratHabitationService;
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

@RestController
@RequestMapping("/api/v1/insurances/home")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contrats Habitation", description = "Gestion des contrats d'assurance habitation")
public class ContratHabitationController {

    private final ContratHabitationService service;

    @Operation(summary = "Créer un contrat habitation")
    @PostMapping
    public ResponseEntity<ContratHabitationResponse> create(@RequestBody ContratHabitationRequest request) {
        ContratHabitationResponse response = service.createContrat(request);
        log.info("Contrat habitation créé avec ID: {}", response.getId());
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Modifier un contrat habitation")
    @PutMapping("/{id}")
    public ResponseEntity<ContratHabitationResponse> update(
            @PathVariable String id,
            @RequestBody ContratHabitationRequest request
    ) {
        ContratHabitationResponse response = service.updateContrat(id, request);
        log.info("Contrat habitation ID={} mis à jour avec succès", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister tous les contrats habitation paginés")
    @GetMapping
    public ResponseEntity<Page<ContratHabitationResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération de tous les contrats habitation, page={}, size={}", page, size);
        List<ContratHabitationResponse> list = service.getAllContrats(page, size);
        log.info("Nombre de contrats habitation récupérés: {}", list.size());
        Page<ContratHabitationResponse> paged = new PageImpl<>(list, PageRequest.of(page, size), list.size());
        return ResponseEntity.ok(paged);
    }

    @Operation(summary = "Supprimer un contrat habitation")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Suppression du contrat habitation ID={}", id);
        service.deleteContrat(id);
        log.info("Contrat habitation ID={} supprimé", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer un contrat habitation par ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContratHabitationResponse> getById(@PathVariable String id) {
        ContratHabitationResponse response = service.getContratById(id);
        log.info("Contrat habitation récupéré avec ID={}", response.getId());
        return ResponseEntity.ok(response);
    }
}
