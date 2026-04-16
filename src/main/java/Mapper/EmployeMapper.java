package Mapper;

import Models.Employe;
import dto.EmployeCreateDTO;
import dto.EmployeDTO;
import dto.EmployeUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeMapper {

    @Mapping(target = "idUtilisateur", ignore = true)
    @Mapping(target = "idEmploye", ignore = true)
    @Mapping(target = "huilerieEmp", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiresAt", ignore = true)
    Employe toEntity(EmployeCreateDTO dto);

    @Mapping(target = "huilerieEmpId", source = "huilerieEmp.idHuilerie")
    EmployeDTO toDTO(Employe entity);

    @Mapping(target = "idUtilisateur", ignore = true)
    @Mapping(target = "idEmploye", ignore = true)
    @Mapping(target = "huilerieEmp", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiresAt", ignore = true)
    void updateFromDTO(EmployeUpdateDTO dto, @MappingTarget Employe entity);
}
