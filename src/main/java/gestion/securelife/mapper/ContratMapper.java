package gestion.securelife.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import gestion.securelife.dto.request.*;
import gestion.securelife.dto.response.*;
import gestion.securelife.entity.*;

@Mapper(componentModel = "spring")
public interface ContratMapper {

    // =========================
    // Mapping ContratAssuranceAuto -> ContratAutoResponse
    // =========================
    @Mapping(target = "typeAssurance", expression = "java(\"AUTO\")")
    ContratAutoResponse toAutoResponse(ContratAssuranceAuto contrat);



    // Mapping ContratAutoRequest -> ContratAssuranceAuto
    ContratAssuranceAuto toAuto(ContratAutoRequest request);

    // Mise à jour d’un ContratAssuranceAuto depuis ContratAutoRequest
    void updateAutoFromRequest(@MappingTarget ContratAssuranceAuto contrat, ContratAutoRequest request);

    // =========================
    // Mapping ContratAssuranceHabitation -> ContratHabitationResponse
    // =========================
    @Mapping(target = "typeAssurance", expression = "java(\"HABITATION\")")
    ContratHabitationResponse toHabitationResponse(ContratAssuranceHabitation contrat);

    ContratAssuranceHabitation toHabitation(ContratHabitationRequest request);

    void updateHabitationFromRequest(@MappingTarget ContratAssuranceHabitation contrat, ContratHabitationRequest request);

    // =========================
    // Mapping ContratAssuranceVie -> ContratVieResponse
    // =========================
    @Mapping(target = "typeAssurance", expression = "java(\"VIE\")")
    ContratVieResponse toVieResponse(ContratAssuranceVie contrat);

    ContratAssuranceVie toVie(ContratVieRequest request);





    void updateVieFromRequest(@MappingTarget ContratAssuranceVie contrat, ContratVieRequest request);

    // =========================
    // Mapping commun Contrat -> ContratResponse avec typeAssurance
    // =========================
    @Mapping(target = "typeAssurance", expression = "java(getTypeAssurance(contrat))")
    ContratResponse toResponse(Contrat contrat);


    default String getTypeAssurance(Contrat contrat) {
        if (contrat instanceof ContratAssuranceAuto) return "AUTO";
        if (contrat instanceof ContratAssuranceHabitation) return "HABITATION";
        if (contrat instanceof ContratAssuranceVie) return "VIE";
        return "UNKNOWN";
    }

    // Mise à jour du status du contrat
    void updateStatusFromRequest(@MappingTarget Contrat contrat, ContratStatusUpdateRequest request);
}
