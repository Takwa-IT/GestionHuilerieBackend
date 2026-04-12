package Mapper;

import Models.MatierePremiere;
import dto.MatierePremiereCreateDTO;
import dto.MatierePremiereDTO;
import dto.MatierePremiereUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MatierePremiereMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "lots", ignore = true)
    @Mapping(target = "machinesAffectees", ignore = true)
    MatierePremiere toEntity(MatierePremiereCreateDTO dto);

    @Mapping(target = "huilerieId", source = "huilerie.idHuilerie")
    MatierePremiereDTO toDTO(MatierePremiere entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "lots", ignore = true)
    @Mapping(target = "machinesAffectees", ignore = true)
    void updateFromDTO(MatierePremiereUpdateDTO dto, @MappingTarget MatierePremiere entity);
}
