package Mapper;

import Models.Entreprise;
import dto.EntrepriseCreateDTO;
import dto.EntrepriseDTO;
import dto.EntrepriseUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EntrepriseMapper {

    @Mapping(target = "idEntreprise", ignore = true)
    @Mapping(target = "huileries", ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    Entreprise toEntity(EntrepriseCreateDTO dto);

    @Mapping(target = "administrateurId", source = "administrateur.idUtilisateur")
    @Mapping(target = "huilerieIds", expression = "java(mapHuilerieIds(entity.getHuileries()))")
    EntrepriseDTO toDTO(Entreprise entity);

    @Mapping(target = "idEntreprise", ignore = true)
    @Mapping(target = "huileries", ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    void updateFromDTO(EntrepriseUpdateDTO dto, @MappingTarget Entreprise entity);

    default List<Long> mapHuilerieIds(List<Models.Huilerie> huileries) {
        if (huileries == null) {
            return null;
        }
        return huileries.stream().map(Models.Huilerie::getIdHuilerie).collect(Collectors.toList());
    }
}
