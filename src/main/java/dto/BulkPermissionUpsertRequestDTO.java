package dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkPermissionUpsertRequestDTO {
    @NotNull
    private Long profilId;

    @Valid
    @NotEmpty
    private List<PermissionItemUpsertDTO> permissions;
}


