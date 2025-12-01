package gestion.securelife.service;

import gestion.securelife.dto.request.DocumentUploadRequest;
import gestion.securelife.dto.response.DocumentResponse;
import gestion.securelife.entity.Contrat;
import gestion.securelife.entity.Document;
import gestion.securelife.entity.User;
import gestion.securelife.entity.enums.Role;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.mapper.DocumentMapper;
import gestion.securelife.repository.ContratRepository;
import gestion.securelife.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // 🔐 Récupérer l'utilisateur connecté
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (User) auth.getPrincipal();
    }

    // Liste des documents d'un contrat
    public List<DocumentResponse> listByContrat(Long contratId) {
        Contrat contrat = contratRepository.findById(String.valueOf(contratId))
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CLIENT
                && !contrat.getEmail().equals(currentUser.getEmail())) {
            throw new AccessDeniedException("Accès refusé aux documents de ce contrat");
        }

        return documentRepository.findByContrat(contrat)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    // Télécharger un document
    public ResponseEntity<byte[]> download(Long id) throws IOException {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));

        Contrat contrat = doc.getContrat();
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CLIENT
                && !contrat.getEmail().equals(currentUser.getEmail())) {
            throw new AccessDeniedException("Accès refusé à ce document");
        }

        Path filePath = Paths.get(doc.getFilePath());
        byte[] fileContent = Files.readAllBytes(filePath);

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileContent);
    }

    // Supprimer un document (ADMIN seulement)
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Seuls les ADMIN peuvent supprimer un document");
        }

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));

        File file = new File(doc.getFilePath());
        if (file.exists()) file.delete();
        documentRepository.delete(doc);
    }

    // Validation du fichier (taille, type, etc.)
    private void validateFile(MultipartFile file, Contrat contrat) {
        if (file == null || file.isEmpty()) throw new RuntimeException("Fichier obligatoire");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new RuntimeException("Format non autorisé, formats valides : PDF, JPG, JPEG, PNG");
        if (file.getSize() > MAX_FILE_SIZE)
            throw new RuntimeException("Fichier trop volumineux (max 5MB)");

        long count = documentRepository.countByContrat(contrat);
        if (count >= 10) throw new RuntimeException("Limite 10 documents atteinte");
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "bin";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String buildUploadFolder() {
        String baseDirectory = System.getProperty("user.dir") + "/uploads";
        LocalDate now = LocalDate.now();
        return baseDirectory + "/" + now.getYear()
                + "/" + String.format("%02d", now.getMonthValue())
                + "/" + String.format("%02d", now.getDayOfMonth());
    }

    // Upload du fichier
    public DocumentResponse upload(Long contratId, DocumentUploadRequest request) throws Exception {
        Contrat contrat = contratRepository.findById(String.valueOf(contratId))
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CLIENT
                && !contrat.getEmail().equals(currentUser.getEmail())) {
            throw new AccessDeniedException("Accès refusé pour l'upload sur ce contrat");
        }

        MultipartFile file = request.getFile();
        validateFile(file, contrat);

        String extension = getExtension(file.getOriginalFilename());
        String generatedName = UUID.randomUUID() + "." + extension;

        String folder = buildUploadFolder();
        Path directoryPath = Paths.get(folder);
        if (Files.notExists(directoryPath)) Files.createDirectories(directoryPath);

        String filePath = folder + "/" + generatedName;
        file.transferTo(new File(filePath));

        Document document = new Document(generatedName, filePath, file.getContentType(),
                file.getSize(), request.getType(), contrat);

        documentRepository.save(document);
        emailService.sendUploadConfirmationEmail(contrat, document);

        return documentMapper.toResponse(document);
    }


}
