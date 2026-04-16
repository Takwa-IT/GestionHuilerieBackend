package dto;

import lombok.Data;

@Data
public class EmployeDTO {
    private Long idUtilisateur;
    private Long idEmploye;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Long huilerieEmpId;
    private Boolean emailVerified;
}
