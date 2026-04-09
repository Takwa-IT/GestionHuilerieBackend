package Mapper;

import Models.LotOlives;
import dto.LotOlivesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LotOlivesMapper {

    @Mapping(target = "matierePremiereId", source = "matierePremiere.id")
    @Mapping(target = "matierePremiereReference", source = "matierePremiere.reference")
    @Mapping(target = "campagneId", source = "campagne.idCampagne")
    LotOlivesDTO toDTO(LotOlives entity);
}
