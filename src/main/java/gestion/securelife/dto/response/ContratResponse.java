package gestion.securelife.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContratResponse {
    private Integer id;
    private String numeroContrat;
    private String nom_complet;
    private String email;
    private Double primeBase;
    private Double primeAnnuelle;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String typeAssurance;
    private Integer client_id;

}
