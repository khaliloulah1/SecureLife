package gestion.securelife.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "contrats_assurance_auto")

@NoArgsConstructor @AllArgsConstructor
public class ContratAssuranceAuto extends Contrat {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String immatriculation;

    @Min(1)
    @Max(50)
    @Column(nullable = false)
    private Integer puissanceFiscale;

    @Min(50)
    @Max(350)
    @Column(nullable = false)
    private Integer bonusMalus;
}
