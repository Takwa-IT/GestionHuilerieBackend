package Controllers;

import Config.UnauthorizedException;
import Services.AuthService;
import dto.AuthResponseDTO;
import dto.LoginRequestDTO;
import dto.MessageResponseDTO;
import dto.RefreshRequestDTO;
import dto.ResetPasswordConfirmDTO;
import dto.ResetPasswordRequestDTO;
import dto.SignupRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getMotDePasse()));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            AuthResponseDTO authResponse = authService.verifyEmail(token);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(ex.getMessage()));
        }
    }

    @PostMapping("/verify-email/resend")
    public ResponseEntity<MessageResponseDTO> resendVerificationEmail(
            @Valid @RequestBody ResetPasswordRequestDTO request
    ) {
        try {
            authService.resendVerificationEmail(request.getEmail());
        } catch (Exception ignored) {
            // Reponse neutre pour ne pas exposer l'existence du compte
        }
        return ResponseEntity.ok(
                new MessageResponseDTO("Si le compte existe et n'est pas verifie, un email a ete renvoye")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
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
        return ResponseEntity.ok(new MessageResponseDTO("Si l'email existe, un lien a ete envoye"));
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmResetPassword(@Valid @RequestBody ResetPasswordConfirmDTO request) {
        try {
            AuthResponseDTO authResponse = authService.confirmResetPassword(
                    request.getToken(),
                    request.getNouveauMotDePasse()
            );
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(ex.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new UnauthorizedException("Token JWT manquant ou invalide pour /api/auth/me");
        }
        return ResponseEntity.ok(authService.me(user.getUsername()));
    }
}