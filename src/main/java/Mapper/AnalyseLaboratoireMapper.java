package Mapper;

import Models.AnalyseLaboratoire;
import dto.AnalyseLaboratoireCreateDTO;
import dto.AnalyseLaboratoireDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalyseLaboratoireMapper {

    @Mapping(target = "lotId", source = "lot.idLot")
    AnalyseLaboratoireDTO toDTO(AnalyseLaboratoire entity);

    @Mapping(target = "idAnalyse", ignore = true)
    @Mapping(target = "lot", ignore = true)
    AnalyseLaboratoire toEntity(AnalyseLaboratoireCreateDTO dto);
}
