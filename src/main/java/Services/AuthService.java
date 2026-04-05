package Services;

import Config.UnauthorizedException;
import Models.Huilerie;
import Models.PasswordResetToken;
import Models.Permission;
import Models.RefreshToken;
import Models.Profil;
import Models.StatutUtilisateur;
import Models.Utilisateur;
import Repositories.HuilerieRepository;
import Repositories.PasswordResetTokenRepository;
import Repositories.PermissionRepository;
import Repositories.ProfilRepository;
import Repositories.RefreshTokenRepository;
import Repositories.UtilisateurRepository;
import dto.AuthPermissionDTO;
import dto.AuthResponseDTO;
import dto.AuthUtilisateurDTO;
import dto.SignupRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final HuilerieRepository huilerieRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PermissionService permissionService;
    private final Optional<JavaMailSender> javaMailSender;

    @Value("${security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${security.reset-password.expiration-minutes:30}")
    private long resetPasswordExpirationMinutes;

    public AuthResponseDTO signup(SignupRequestDTO request) {
        utilisateurRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email deja utilise");
                });

        Profil profil = profilRepository.findByNom("RESPONSABLE_PRODUCTION")
                .orElseThrow(() -> new IllegalArgumentException("Profil par defaut introuvable"));
        Huilerie huilerie = huilerieRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aucune huilerie disponible"));

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setProfil(profil);
        utilisateur.setHuilerie(huilerie);
        utilisateur.setActif(StatutUtilisateur.ACTIF);

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        String token = jwtService.generateToken(saved);
        RefreshToken refreshToken = createRefreshToken(saved);
        return buildAuthResponse(saved, token, refreshToken.getToken());
    }

    public AuthResponseDTO login(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe invalide"));

        if (utilisateur.getActif() != StatutUtilisateur.ACTIF) {
            throw new UnauthorizedException("Utilisateur inactif");
        }

        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        String token = jwtService.generateToken(utilisateur);
        RefreshToken refreshToken = createRefreshToken(utilisateur);

        return buildAuthResponse(utilisateur, token, refreshToken.getToken());
    }

    public AuthResponseDTO refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalide"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new UnauthorizedException("Refresh token expire");
        }

        Utilisateur utilisateur = refreshToken.getUtilisateur();
        String token = jwtService.generateToken(utilisateur);
        return buildAuthResponse(utilisateur, token, refreshToken.getToken());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    permissionService.evictUserPermissions(token.getUtilisateur().getIdUtilisateur());
                });
    }

    public void requestResetPassword(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
        if (utilisateur == null) {
            return;
        }

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUtilisateur(utilisateur);
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(resetPasswordExpirationMinutes));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        javaMailSender.ifPresent(sender -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(utilisateur.getEmail());
            message.setSubject("Reinitialisation mot de passe");
            message.setText("Votre token temporaire: " + resetToken.getToken());
            sender.send(message);
        });
    }

    public void confirmResetPassword(String tokenValue, String nouveauMotDePasse) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token de reinitialisation invalide"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de reinitialisation expire");
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUtilisateurAndRevokedFalse(utilisateur);
        for (RefreshToken refreshToken : activeTokens) {
            refreshToken.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO me(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        String token = jwtService.generateToken(utilisateur);
        String refreshToken = createRefreshToken(utilisateur).getToken();
        return buildAuthResponse(utilisateur, token, refreshToken);
    }

    private RefreshToken createRefreshToken(Utilisateur utilisateur) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUtilisateur(utilisateur);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponseDTO buildAuthResponse(Utilisateur utilisateur, String token, String refreshToken) {
        AuthUtilisateurDTO authUtilisateurDTO = new AuthUtilisateurDTO();
        authUtilisateurDTO.setId(utilisateur.getIdUtilisateur());
        authUtilisateurDTO.setNom(utilisateur.getNom());
        authUtilisateurDTO.setPrenom(utilisateur.getPrenom());
        authUtilisateurDTO.setEmail(utilisateur.getEmail());
        authUtilisateurDTO.setProfil(utilisateur.getProfil().getNom());

        List<AuthPermissionDTO> permissions = permissionRepository.findByProfilIdWithModule(utilisateur.getProfil().getIdProfil())
                .stream()
                .map(this::toAuthPermission)
                .toList();

        AuthResponseDTO responseDTO = new AuthResponseDTO();
        responseDTO.setToken(token);
        responseDTO.setRefreshToken(refreshToken);
        responseDTO.setUtilisateur(authUtilisateurDTO);
        responseDTO.setPermissions(permissions);
        return responseDTO;
    }

    private AuthPermissionDTO toAuthPermission(Permission permission) {
        AuthPermissionDTO dto = new AuthPermissionDTO();
        dto.setModule(permission.getModule().getNom());
        dto.setCanCreate(permission.getCanCreate());
        dto.setCanRead(permission.getCanRead());
        dto.setCanUpdate(permission.getCanUpdate());
        dto.setCanDelete(permission.getCanDelete());
        dto.setCanExecuted(permission.getCanExecuted());
        return dto;
    }
}