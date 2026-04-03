package dto;

import lombok.Data;

import java.util.List;

@Data
public class GuideProductionDTO {

    private Long idGuideProduction;
    private String nom;
    private String code;
    private String description;
    private String dateCreation;
    private Long huilerieId;
    private String huilerieNom;
    private List<EtapeProductionDTO> etapes;
}