package Mapper;

import Models.CampagneOlives;
import Models.Huilerie;
import dto.CampagneOlivesCreateDTO;
import dto.CampagneOlivesDTO;
import dto.CampagneOlivesUpdateDTO;
import org.springframework.stereotype.Component;

@Component
public class CampagneOlivesMapper {

    public CampagneOlivesDTO toDTO(CampagneOlives entity) {
        if (entity == null) {
            return null;
        }

        CampagneOlivesDTO dto = new CampagneOlivesDTO();
        dto.setIdCampagne(entity.getIdCampagne());
        dto.setReference(entity.getReference());
        dto.setAnnee(entity.getAnnee());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());

        Huilerie huilerie = entity.getHuilerie();
        if (huilerie != null) {
            dto.setHuilerieId(huilerie.getIdHuilerie());
            dto.setHuilerieNom(huilerie.getNom());
        }

        return dto;
    }

    public CampagneOlives toEntity(CampagneOlivesCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        CampagneOlives entity = new CampagneOlives();
        entity.setAnnee(dto.getAnnee());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        return entity;
    }

    public void updateFromDTO(CampagneOlivesUpdateDTO dto, CampagneOlives entity) {
        if (dto == null) {
            return;
        }

        if (dto.getDateDebut() != null) {
            entity.setDateDebut(dto.getDateDebut());
        }
        if (dto.getDateFin() != null) {
            entity.setDateFin(dto.getDateFin());
        }
    }
}
