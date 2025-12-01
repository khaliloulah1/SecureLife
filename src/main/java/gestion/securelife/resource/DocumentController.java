package gestion.securelife.resource;

import gestion.securelife.dto.request.DocumentUploadRequest;
import gestion.securelife.dto.response.DocumentResponse;
import gestion.securelife.entity.User;
import gestion.securelife.entity.enums.DocumentType;
import gestion.securelife.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    // 🔐 Méthode utilitaire pour récupérer user + rôle dans les logs
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (User) auth.getPrincipal();
    }

    /**
     * Upload de document
     */
    @PostMapping("/insurances/{id}/documents")
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable Long id,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        User user = getCurrentUser();

        log.info("📤 [UPLOAD] User={} | Role={} | ContratID={} | TypeDoc={} | File={}",
                user.getEmail(), user.getRole(), id, type, file.getOriginalFilename());

        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setType(DocumentType.valueOf(type));
        req.setFile(file);

        DocumentResponse response = documentService.upload(id, req);

        log.info("✅ [UPLOAD SUCCESS] DocumentID={} uploadé par {} ({})",
                response.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * Liste des documents d'un contrat
     */
    @GetMapping("/insurances/{id}/documents")
    public ResponseEntity<List<DocumentResponse>> list(@PathVariable Long id) {

        User user = getCurrentUser();

        log.info("📁 [LIST] User={} | Role={} | ContratID={}",
                user.getEmail(), user.getRole(), id);

        List<DocumentResponse> documents = documentService.listByContrat(id);

        log.info("📄 [LIST RESULT] User={} | ContratID={} | NombreDocuments={}",
                user.getEmail(), id, documents.size());

        return ResponseEntity.ok(documents);
    }

    /**
     * Télécharger un document
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Exception {

        User user = getCurrentUser();

        log.info("📥 [DOWNLOAD] User={} | Role={} | DocumentID={}",
                user.getEmail(), user.getRole(), id);

        ResponseEntity<byte[]> responseEntity = documentService.download(id);

        log.info("✅ [DOWNLOAD SUCCESS] User={} | DocumentID={} | Taille={} bytes",
                user.getEmail(), id, responseEntity.getBody().length);

        return responseEntity;
    }

    /**
     * Supprimer un document
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        User user = getCurrentUser();

        log.info("🗑 [DELETE] User={} | Role={} | DocumentID={}",
                user.getEmail(), user.getRole(), id);

        documentService.delete(id);

        log.info("🗑✅ [DELETE SUCCESS] User={} | Role={} | DocumentID={} supprimé",
                user.getEmail(), user.getRole(), id);

        return ResponseEntity.noContent().build();
    }
}
