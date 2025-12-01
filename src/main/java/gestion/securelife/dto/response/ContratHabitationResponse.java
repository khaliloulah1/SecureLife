package gestion.securelife.dto.response;

// dto/response/ContratHabitationResponse.java

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContratHabitationResponse extends ContratResponse {
    private String adresse;
    private Double superficie;
    private String zoneRisque;
}
