package gestion.securelife.resource;

import gestion.securelife.dto.request.ContratVieRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratVieResponse;
import gestion.securelife.service.interf.ContratVieService;
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
@RequestMapping("/api/v1/insurances/life")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contrats Vie", description = "Gestion des contrats d'assurance vie")
public class ContratVieController {

    private final ContratVieService service;

    @Operation(summary = "Créer un contrat vie")
    @PostMapping
    public ResponseEntity<ContratVieResponse> create(@RequestBody ContratVieRequest request) {
        ContratVieResponse response = service.createContrat(request);
        log.info("Contrat vie créé avec ID: {}", response.getId());
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Modifier un contrat vie")
    @PutMapping("/{id}")
    public ResponseEntity<ContratVieResponse> update(
            @PathVariable String id,
            @RequestBody ContratVieRequest request
    ) {
        ContratVieResponse response = service.updateContrat(id, request);
        log.info("Contrat vie ID={} mis à jour avec succès", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister tous les contrats vie paginés")
    @GetMapping
    public ResponseEntity<Page<ContratVieResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération de tous les contrats vie, page={}, size={}", page, size);
        List<ContratVieResponse> list = service.getAllContrats(page, size);
        log.info("Nombre de contrats vie récupérés: {}", list.size());
        Page<ContratVieResponse> paged = new PageImpl<>(list, PageRequest.of(page, size), list.size());
        return ResponseEntity.ok(paged);
    }

    @Operation(summary = "Supprimer un contrat vie")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteContrat(id);
        log.info("Contrat vie ID={} supprimé", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer un contrat vie par ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContratVieResponse> getById(@PathVariable String id) {
        ContratVieResponse response = service.getContratById(id);
        log.info("Contrat vie récupéré avec ID={}", response.getId());
        return ResponseEntity.ok(response);
    }
}
