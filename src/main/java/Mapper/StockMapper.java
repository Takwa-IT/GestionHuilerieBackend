package Mapper;

import Models.Stock;
import dto.StockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "huilerieId", source = "huilerie.idHuilerie")
    @Mapping(target = "huilerieNom", source = "huilerie.nom")
    @Mapping(target = "referenceId", source = "lotOlives.idLot")
    StockDTO toDTO(Stock entity);
}
