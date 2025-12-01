package gestion.securelife.service.interf;

import gestion.securelife.dto.response.ContratResponse;
import gestion.securelife.dto.response.ContratVieResponse;

import java.util.List;

public interface ContratRechercheService {
    List<ContratResponse> getAllContrats(int page, int size);

}
