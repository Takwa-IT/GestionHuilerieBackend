package dto;

import lombok.Data;

@Data
public class AuthPermissionDTO {
    private String module;
    private Boolean canCreate;
    private Boolean canRead;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canExecuted;
}