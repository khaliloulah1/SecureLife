package gestion.securelife.dto.response;

// dto/response/ContratAutoResponse.java

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContratAutoResponse extends ContratResponse {
    private String immatriculation;
    private Integer puissanceFiscale;
    private Integer bonusMalus;
}
