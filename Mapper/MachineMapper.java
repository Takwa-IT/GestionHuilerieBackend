package Mapper;

import Models.Machine;
import dto.MachineCreateDTO;
import dto.MachineDTO;
import dto.MachineUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MachineMapper {

    @Mapping(target = "idMachine", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    Machine toEntity(MachineCreateDTO dto);

    @Mapping(target = "huilerieId", source = "huilerie.idHuilerie")
    MachineDTO toDTO(Machine entity);

    @Mapping(target = "idMachine", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    void updateFromDTO(MachineUpdateDTO dto, @MappingTarget Machine entity);
}
