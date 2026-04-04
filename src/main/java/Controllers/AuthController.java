package Controllers;

import Services.AuthService;
import dto.AuthResponseDTO;
import dto.LoginRequestDTO;
import dto.MessageResponseDTO;
import dto.RefreshRequestDTO;
import dto.ResetPasswordConfirmDTO;
import dto.ResetPasswordRequestDTO;
import dto.TokenResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getMotDePasse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<MessageResponseDTO> requestResetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.requestResetPassword(request.getEmail());
        return new ResponseEntity<>(new MessageResponseDTO("Si l'email existe, un token a ete envoye"), HttpStatus.OK);
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<MessageResponseDTO> confirmResetPassword(@Valid @RequestBody ResetPasswordConfirmDTO request) {
        authService.confirmResetPassword(request.getToken(), request.getNouveauMotDePasse());
        return new ResponseEntity<>(new MessageResponseDTO("Mot de passe reinitialise"), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.me(user.getUsername()));
    }
}