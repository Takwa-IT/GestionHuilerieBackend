package dto;

import lombok.Data;

@Data
public class CampagneOlivesDTO {
    private Long idCampagne;
    private String reference;
    private String annee;
    private String dateDebut;
    private String dateFin;
    private Long huilerieId;
    private String huilerieNom;
}
