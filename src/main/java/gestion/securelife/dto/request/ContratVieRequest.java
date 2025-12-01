package gestion.securelife.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratVieRequest {

    @NotBlank(message = "Le nom_complet est obligatoire")
    private String nom_complet;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotNull(message = "La prime de base est obligatoire")
    @Positive(message = "La prime de base doit être strictement positive")
    private Double primeBase;

    @NotNull(message = "L'âge de l'assuré est obligatoire")
    @Min(value = 18, message = "Âge minimum 18 ans")
    @Max(value = 80, message = "Âge maximum 80 ans")
    private Integer ageAssure;

    @NotNull(message = "Le capital garanti est obligatoire")
    @Min(value = 10000, message = "Capital garanti minimum 10 000€")
    private Double capitalGaranti;

    @NotBlank(message = "Le bénéficiaire est obligatoire")
    private String beneficiaire;
    private Integer client_id;

}
