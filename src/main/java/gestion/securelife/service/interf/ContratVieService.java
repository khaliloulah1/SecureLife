package gestion.securelife.service.interf;


import gestion.securelife.dto.request.ContratVieRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratVieResponse;

import java.util.List;

public interface ContratVieService {
    ContratVieResponse createContrat(ContratVieRequest request);
    ContratVieResponse getContratById(String id);
    ContratVieResponse updateContrat(String id, ContratVieRequest request);
    void deleteContrat(String id);
    List<ContratVieResponse> getAllContrats(int page, int size);
}
