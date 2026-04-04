package dto;

import lombok.Data;

import java.util.List;

@Data
public class GuideProductionDTO {

    private Long idGuideProduction;
    private String reference;
    private String nom;
    private String description;
    private String dateCreation;
    private Long huilerieId;
    private String huilerieNom;
    private List<EtapeProductionDTO> etapes;
}