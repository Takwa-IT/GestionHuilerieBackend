package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfilDTO {
    private Long idProfil;
    private String nom;
    private String description;
    private LocalDateTime dateCreation;
}


