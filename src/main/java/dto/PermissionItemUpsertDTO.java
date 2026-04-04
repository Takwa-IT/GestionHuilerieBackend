package dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionItemUpsertDTO {
    @NotNull
    private Long moduleId;

    @NotNull
    private Boolean canCreate;

    @NotNull
    private Boolean canRead;

    @NotNull
    private Boolean canUpdate;

    @NotNull
    private Boolean canDelete;

    @NotNull
    private Boolean canExecuted;
}