package gestion.securelife.resource;

import gestion.securelife.dto.request.ContratAutoRequest;
import gestion.securelife.dto.response.ContratAutoResponse;
import gestion.securelife.service.interf.ContratAutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/insurances/auto")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contrats Auto", description = "Gestion des contrats d'assurance automobile")
public class ContratAutoController {

    private final ContratAutoService service;

    @PostMapping
    @Operation(summary = "Créer un contrat auto")
    public ResponseEntity<ContratAutoResponse> create(@RequestBody @Valid ContratAutoRequest request) {
        log.info("Création d'un contrat auto pour: {}", request.getNom_complet());
        ContratAutoResponse response = service.createContrat(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un contrat auto")
    public ResponseEntity<ContratAutoResponse> update(@PathVariable String id,
                                                      @RequestBody @Valid ContratAutoRequest request) {
        log.info("Modification du contrat ID: {}", id);
        ContratAutoResponse response = service.updateContrat(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un contrat auto")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Suppression du contrat ID: {}", id);
        service.deleteContrat(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un contrat auto par ID")
    public ResponseEntity<ContratAutoResponse> getById(@PathVariable String id) {
        log.info("Consultation du contrat ID: {}", id);
        return ResponseEntity.ok(service.getContratById(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les contrats auto paginés")
    public ResponseEntity<Page<ContratAutoResponse>> getAll(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        List<ContratAutoResponse> list = service.getAllContrats(page, size);
        return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
    }
}
