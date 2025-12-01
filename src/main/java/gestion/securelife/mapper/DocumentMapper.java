package gestion.securelife.mapper;

import gestion.securelife.dto.response.DocumentResponse;
import gestion.securelife.dto.response.DocumentResponse;
import gestion.securelife.entity.Document;
import gestion.securelife.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "downloadUrl",
            expression = "java(\"/api/v1/documents/\" + doc.getId())")

    @Mapping(target = "contratId",
            expression = "java(doc.getContrat().getId())")
    DocumentResponse toResponse(Document doc);
}
