package gestion.securelife.service.interf;

import gestion.securelife.dto.request.ContratAutoRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratAutoResponse;

import java.util.List;




public interface ContratAutoService {

    ContratAutoResponse createContrat(ContratAutoRequest request);

    ContratAutoResponse getContratById(String id);

    ContratAutoResponse updateContrat(String id, ContratAutoRequest request);

    void deleteContrat(String id);


    List<ContratAutoResponse> getAllContrats(int page, int size);
}
