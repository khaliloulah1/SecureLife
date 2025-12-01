package gestion.securelife.service;

import gestion.securelife.dto.request.DocumentUploadRequest;
import gestion.securelife.dto.response.DocumentResponse;
import gestion.securelife.entity.Contrat;
import gestion.securelife.entity.Document;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.mapper.DocumentMapper;
import gestion.securelife.repository.ContratRepository;
import gestion.securelife.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ContratRepository contratRepository;
    private final DocumentMapper documentMapper;
    private final EmailService emailService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Liste des documents d'un contrat
    public List<DocumentResponse> listByContrat(Long contratId) {
        Contrat contrat = contratRepository.findById(String.valueOf(contratId))
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));

        return documentRepository.findByContrat(contrat)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    // Télécharger un document
    public ResponseEntity<byte[]> download(Long id) throws IOException {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));

        File file = new File(doc.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Fichier introuvable sur le disque");
        }

        // Lire le contenu du fichier
        Path filePath = Paths.get(doc.getFilePath());
        byte[] fileContent = Files.readAllBytes(filePath);

        // Déterminer le type MIME du fichier
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream"; // Défaut si non détecté
        }

        // Retourner la réponse avec le fichier et les bons en-têtes pour forcer le téléchargement
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileContent);
    }

    // Supprimer un document
    public void delete(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));

        // Supprimer le fichier du disque
        File file = new File(doc.getFilePath());
        if (file.exists()) file.delete();

        documentRepository.delete(doc);
    }

    // Validation du fichier (taille, type, etc.)
    private void validateFile(MultipartFile file, Contrat contrat) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier obligatoire");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Format non autorisé, formats valides : PDF, JPG, JPEG, PNG");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Fichier trop volumineux (max 5MB)");
        }

        long count = documentRepository.countByContrat(contrat);
        if (count >= 10) {
            throw new RuntimeException("Limite 10 documents atteinte");
        }
    }

    // Récupérer l'extension du fichier
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    // Créer le dossier de stockage pour l'upload avec un chemin relatif
    private String buildUploadFolder() {
        // Chemin relatif basé sur le répertoire du projet
        String baseDirectory = System.getProperty("user.dir") + "/uploads";
        System.out.println("Base directory for uploads: " + baseDirectory); // Log pour déboguer

        // Structure du chemin avec année, mois, jour
        LocalDate now = LocalDate.now();
        return baseDirectory + "/" + now.getYear()
                + "/" + String.format("%02d", now.getMonthValue())
                + "/" + String.format("%02d", now.getDayOfMonth());
    }

    // Upload du fichier
    public DocumentResponse upload(Long contratId, DocumentUploadRequest request) throws Exception {
        Contrat contrat = contratRepository.findById(String.valueOf(contratId))
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));

        MultipartFile file = request.getFile();
        validateFile(file, contrat);

        String extension = getExtension(file.getOriginalFilename());
        String generatedName = UUID.randomUUID() + "." + extension;

        String folder = buildUploadFolder();
        Path directoryPath = Paths.get(folder);

        // Log pour déboguer la création du dossier
        System.out.println("Creating directory: " + folder);

        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);  // Crée le répertoire s'il n'existe pas
        }

        String filePath = folder + "/" + generatedName;
        file.transferTo(new File(filePath));  // Sauvegarde le fichier dans le chemin spécifié

        // Sauvegarde du document dans la base de données
        Document document = new Document(
                generatedName,
                filePath,
                file.getContentType(),
                file.getSize(),
                request.getType(),
                contrat
        );

        documentRepository.save(document);

        // Envoi de l'email de confirmation
        sendUploadConfirmationEmail(contrat, generatedName, document);

        return documentMapper.toResponse(document);
    }

    // Envoi de l'email de confirmation après l'upload
    @Async
    protected void sendUploadConfirmationEmail(Contrat contrat, String generatedName, Document document) {
        Context context = new Context();
        context.setVariable("nomClient", contrat.getNom_complet());
        context.setVariable("numeroContrat", contrat.getNumeroContrat());
        context.setVariable("fileName", generatedName);

        emailService.sendEmail(
                contrat.getEmail(),
                "Document reçu - SecureLife",
                "document_uploaded",
                context
        );
    }
}
