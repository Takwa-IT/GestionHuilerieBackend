package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionDTO {
    private Long idPrivilege;
    private Boolean canCreate;
    private Boolean canRead;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canExecuted;
    private String description;
    private LocalDateTime dateCreation;
    private Long profilId;
    private Long moduleId;
}