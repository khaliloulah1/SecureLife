package gestion.securelife.dto.response;

// dto/response/ContratVieResponse.java

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContratVieResponse extends ContratResponse {
    private Integer ageAssure;
    private Double capitalGaranti;
    private String beneficiaire;
}
