package gestion.securelife.entity;
import gestion.securelife.entity.enums.ContratStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "contrats_assurance_habitation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratAssuranceHabitation extends Contrat {

    @NotBlank
    @Column(nullable = false)
    private String adresse;

    @Min(10)
    @Column(nullable = false)
    private Double superficie;

    @NotBlank
    @Column(nullable = false)
    private String zoneRisque;
}
