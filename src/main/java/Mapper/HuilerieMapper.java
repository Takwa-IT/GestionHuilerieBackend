package Mapper;

import Models.Huilerie;
import dto.HuilerieCreateDTO;
import dto.HuilerieDTO;
import dto.HuilerieUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HuilerieMapper {

    @Mapping(target = "idHuilerie", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "machines", ignore = true)
    @Mapping(target = "lots", ignore = true)
    @Mapping(target = "campagnesOlives", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    Huilerie toEntity(HuilerieCreateDTO dto);

    @Mapping(target = "entrepriseId", source = "entreprise.idEntreprise")
    HuilerieDTO toDTO(Huilerie entity);

    @Mapping(target = "idHuilerie", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "machines", ignore = true)
    @Mapping(target = "lots", ignore = true)
    @Mapping(target = "campagnesOlives", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    void updateFromDTO(HuilerieUpdateDTO dto, @MappingTarget Huilerie entity);
}
