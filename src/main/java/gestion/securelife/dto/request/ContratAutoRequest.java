package gestion.securelife.dto.request;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratAutoRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    private String nom_complet;
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotNull(message = "La prime de base est obligatoire")
    @Positive(message = "La prime de base doit être strictement positive")
    private Double primeBase;

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    @NotNull(message = "La puissance fiscale est obligatoire")
    @Min(value = 1, message = "Puissance fiscale minimum 1 CV")
    @Max(value = 50, message = "Puissance fiscale maximum 50 CV")
    private Integer puissanceFiscale;

    @NotNull(message = "Le bonus/malus est obligatoire")
    @Min(value = 50, message = "Bonus/Malus minimum 50%")
    @Max(value = 350, message = "Bonus/Malus maximum 350%")
    private Integer bonusMalus;

}
