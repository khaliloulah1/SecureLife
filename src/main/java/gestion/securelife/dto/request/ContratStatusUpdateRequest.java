package gestion.securelife.dto.request;

import gestion.securelife.entity.enums.ContratStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratStatusUpdateRequest {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private ContratStatus status;
}
