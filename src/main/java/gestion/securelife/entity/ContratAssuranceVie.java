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
@Table(name = "contrats_assurance_vie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratAssuranceVie extends Contrat {

    @Min(18)
    @Max(80)
    @Column(nullable = false)
    private Integer ageAssure;

    @Min(10000)
    @Column(nullable = false)
    private Double capitalGaranti;

    @NotBlank
    @Column(nullable = false)
    private String beneficiaire;
}
