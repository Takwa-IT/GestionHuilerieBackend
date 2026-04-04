package dto;

import Models.StatutUtilisateur;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UtilisateurStatusUpdateDTO {
    @NotNull
    private StatutUtilisateur actif;
}