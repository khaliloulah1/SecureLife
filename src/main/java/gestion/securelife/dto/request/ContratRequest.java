package gestion.securelife.dto.request;

// dto/request/ContratRequest.java

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratRequest {
    @NotBlank(message = "nom_complet obligatoire")
    private String nom_complet;

    @NotBlank
    @Email(message = "Email invalide")
    private String email;

    @NotNull(message = "Prime de base obligatoire")
    @Positive(message = "Prime de base doit être positive")
    private Double primeBase;

}
