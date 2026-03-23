package Mapper;

import Models.StockMovement;
import dto.StockMovementCreateDTO;
import dto.StockMovementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(target = "idStockMovement", ignore = true)
    @Mapping(target = "stock", ignore = true)
    StockMovement toEntity(StockMovementCreateDTO dto);

    @Mapping(target = "lotId", source = "stock.lotOlives.idLot")
    StockMovementDTO toDTO(StockMovement entity);
}
