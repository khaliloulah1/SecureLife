package gestion.securelife.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import gestion.securelife.entity.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "assurance_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;  // nom généré avec UUID

    @Column(nullable = false)
    private String filePath;  // uploads/2025/01/15/xxxx.pdf

    @Column(nullable = false)
    private String fileType;  // Mime type : application/pdf, image/png...

    @Column(nullable = false)
    private Long fileSize;    // max 5 MB

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    @JsonBackReference
    private Contrat contrat;

    @PrePersist
    public void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Constructeur sans contrat (optionnel)
    public Document(String fileName, String filePath, String fileType, Long fileSize, DocumentType documentType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.documentType = documentType;
        this.uploadedAt = LocalDateTime.now();
    }

    // Constructeur complet
    public Document(String fileName, String filePath, String fileType, Long fileSize,
                             DocumentType documentType, Contrat contrat) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.documentType = documentType;
        this.contrat = contrat;
        this.uploadedAt = LocalDateTime.now();
    }
}
