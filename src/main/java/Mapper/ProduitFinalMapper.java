package Mapper;

import Models.ProduitFinal;
import dto.ProduitFinalCreateDTO;
import dto.ProduitFinalDTO;
import dto.ProduitFinalUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProduitFinalMapper {

    @Mapping(target = "idProduit", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "executionProduction", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    ProduitFinal toEntity(ProduitFinalCreateDTO dto);

    @Mapping(target = "executionProductionId", source = "executionProduction.idExecutionProduction")
    ProduitFinalDTO toDTO(ProduitFinal entity);

    @Mapping(target = "idProduit", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "executionProduction", ignore = true)
    @Mapping(target = "executionProductionId", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    void updateFromDTO(ProduitFinalUpdateDTO dto, @MappingTarget ProduitFinal entity);
}
