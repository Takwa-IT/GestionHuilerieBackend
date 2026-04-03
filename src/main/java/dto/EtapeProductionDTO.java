package dto;

import lombok.Data;

import java.util.List;

@Data
public class EtapeProductionDTO {

    private Long idEtapeProduction;
    private String nom;
    private Integer ordre;
    private String description;
    private List<ParametreEtapeDTO> parametres;
}