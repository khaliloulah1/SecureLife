package gestion.securelife.resource;

import gestion.securelife.dto.request.DocumentUploadRequest;
import gestion.securelife.dto.response.DocumentResponse;
import gestion.securelife.service.DocumentService;
import gestion.securelife.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final EmailService emailService;

    /**
     * Upload de document
     */
    @PostMapping("/insurances/{id}/documents")
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable Long id,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        log.info("Upload de document pour le contrat ID={}", id);
        log.info("Type de document: {}", type);
        log.info("Nom du fichier: {}", file.getOriginalFilename());
        log.info("Type MIME du fichier: {}", file.getContentType());
        log.info("Taille du fichier: {} bytes", file.getSize());

        // Création de la requête pour l'upload
        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setType(Enum.valueOf(gestion.securelife.entity.enums.DocumentType.class, type));
        req.setFile(file);

        // Appel du service pour traiter l'upload
        DocumentResponse response = documentService.upload(id, req);
        log.info("Document uploadé avec succès, ID document={}", response.getId());

        // Envoi de l'email de confirmation après upload réussi
        emailService.sendDocumentUploadedEmail(response);
        log.info("Email de confirmation envoyé pour le document ID={}", response.getId());

        // Retourner la réponse de l'upload
        return ResponseEntity.ok(response);
    }

    /**
     * Liste des documents d'un contrat
     */
    @GetMapping("/insurances/{id}/documents")
    public ResponseEntity<List<DocumentResponse>> list(@PathVariable Long id) {
        log.info("Liste des documents pour le contrat ID={}", id);
        List<DocumentResponse> documents = documentService.listByContrat(id);
        log.info("Nombre de documents récupérés: {}", documents.size());
        return ResponseEntity.ok(documents);
    }

    /**
     * Télécharger un document
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Exception {
        log.info("Téléchargement du document ID={}", id);
        ResponseEntity<byte[]> responseEntity = documentService.download(id);
        log.info("Document ID={} téléchargé, taille={}", id, responseEntity.getBody().length);
        return responseEntity;
    }

    /**
     * Supprimer un document
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Suppression du document ID={}", id);
        documentService.delete(id);
        log.info("Document ID={} supprimé avec succès", id);
        return ResponseEntity.noContent().build();
    }
}
