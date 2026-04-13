package Mapper;

import Models.StockMovement;
import dto.StockMovementCreateDTO;
import dto.StockMovementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(target = "idStockMovement", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "stock", ignore = true)
    StockMovement toEntity(StockMovementCreateDTO dto);

    @Mapping(target = "id", source = "idStockMovement")
    @Mapping(target = "huilerieId", source = "stock.huilerie.idHuilerie")
    @Mapping(target = "huilerieNom", source = "stock.huilerie.nom")
    @Mapping(target = "referenceId", source = "stock.lotOlives.idLot")
    StockMovementDTO toDTO(StockMovement entity);
}
