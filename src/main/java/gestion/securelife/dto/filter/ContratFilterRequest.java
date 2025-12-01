package gestion.securelife.dto.filter;


import gestion.securelife.entity.enums.ContratStatus;
import lombok.Data;

@Data
public class ContratFilterRequest {
    private String clientNom_complet;

    private String clientEmail;
    private String type;
    private ContratStatus status;
    private Double primeMin;
    private Double primeMax;


}
