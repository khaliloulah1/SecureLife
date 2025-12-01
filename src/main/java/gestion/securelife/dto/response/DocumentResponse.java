package gestion.securelife.dto.response;

import gestion.securelife.entity.enums.DocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private DocumentType documentType;
    private LocalDateTime uploadedAt;
    private Integer contratId;
    private String downloadUrl;

}
