package gestion.securelife.dto.request;

import gestion.securelife.entity.enums.DocumentType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRequest {
    private DocumentType type;
    private MultipartFile file;
}
