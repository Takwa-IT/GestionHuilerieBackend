package Mapper;

import Models.Pesee;
import dto.PeseeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PeseeMapper {

    @Mapping(target = "idPesee", source = "id")
    @Mapping(target = "lotId", source = "lot.idLot")
    PeseeDTO toDTO(Pesee entity);
}
