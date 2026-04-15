package dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private AuthUtilisateurDTO utilisateur;
    private List<AuthPermissionDTO> permissions;
}


