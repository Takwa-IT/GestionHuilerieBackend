package dto;

import lombok.Data;

@Data
public class PermissionModuleDTO {
    private Long idPrivilege;
    private Long moduleId;
    private String moduleNom;
    private Boolean canCreate;
    private Boolean canRead;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canExecuted;
}


