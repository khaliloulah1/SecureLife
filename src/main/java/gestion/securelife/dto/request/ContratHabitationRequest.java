package gestion.securelife.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratHabitationRequest {

    @NotBlank(message = "Le nom_complet est obligatoire")
    private String nom_complet;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotNull(message = "La prime de base est obligatoire")
    @Positive(message = "La prime de base doit être strictement positive")
    private Double primeBase;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotNull(message = "La superficie est obligatoire")
    @Min(value = 10, message = "Superficie minimum 10 m²")
    private Double superficie;

    @NotBlank(message = "La zone à risque est obligatoire")
    private String zoneRisque;
    private Integer client_id;

}
