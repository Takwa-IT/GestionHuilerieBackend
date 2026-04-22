package dto;

import lombok.Data;

@Data
public class ValeurReelleParametreDTO {
    private Long parametreEtapeId;
    private String parametreEtapeNom;
    private String valeurEstime;
    private String valeurReelle;
}