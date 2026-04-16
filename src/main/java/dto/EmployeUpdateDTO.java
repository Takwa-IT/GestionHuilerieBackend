package dto;

import lombok.Data;

@Data
public class EmployeUpdateDTO {
    private String nom;
    private String prenom;
    private String telephone;
    private Long huilerieEmpId;
}
