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

    @Mapping(target = "entreprise", ignore = true) // géré dans le service
    Huilerie toEntity(HuilerieCreateDTO dto);

    HuilerieDTO toDTO(Huilerie entity);

    @Mapping(target = "entreprise", ignore = true)
    void updateFromDTO(HuilerieUpdateDTO dto, @MappingTarget Huilerie entity);
}