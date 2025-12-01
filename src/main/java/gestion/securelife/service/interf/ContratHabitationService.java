package gestion.securelife.service.interf;

import gestion.securelife.dto.request.ContratHabitationRequest;
import gestion.securelife.dto.request.ContratStatusUpdateRequest;
import gestion.securelife.dto.response.ContratHabitationResponse;

import java.util.List;

public interface ContratHabitationService {
    ContratHabitationResponse createContrat(ContratHabitationRequest request);
    ContratHabitationResponse getContratById(String id);
    ContratHabitationResponse updateContrat(String id, ContratHabitationRequest request);
    void deleteContrat(String id);
    List<ContratHabitationResponse> getAllContrats(int page, int size);
}
