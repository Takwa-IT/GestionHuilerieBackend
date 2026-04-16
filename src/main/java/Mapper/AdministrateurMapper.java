package Mapper;

import Models.Administrateur;
import dto.AdministrateurCreateDTO;
import dto.AdministrateurDTO;
import dto.AdministrateurUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdministrateurMapper {

    @Mapping(target = "idUtilisateur", ignore = true)
    @Mapping(target = "idAdministrateur", ignore = true)
    @Mapping(target = "entrepriseAdmin", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiresAt", ignore = true)
    Administrateur toEntity(AdministrateurCreateDTO dto);

    @Mapping(target = "entrepriseAdminId", source = "entrepriseAdmin.idEntreprise")
    AdministrateurDTO toDTO(Administrateur entity);

    @Mapping(target = "idUtilisateur", ignore = true)
    @Mapping(target = "idAdministrateur", ignore = true)
    @Mapping(target = "entrepriseAdmin", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "huilerie", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiresAt", ignore = true)
    void updateFromDTO(AdministrateurUpdateDTO dto, @MappingTarget Administrateur entity);
}
