package Mapper;

import Models.Stock;
import dto.StockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "huilerieId", source = "lotOlives.huilerie.idHuilerie")
    @Mapping(target = "huilerieNom", source = "lotOlives.huilerie.nom")
    @Mapping(target = "referenceId", source = "lotOlives.idLot")
    @Mapping(target = "matierePremiereId", ignore = true)
    @Mapping(target = "lotReferences", ignore = true)
    StockDTO toDTO(Stock entity);
}
