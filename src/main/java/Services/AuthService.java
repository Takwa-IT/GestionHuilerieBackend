package Services;

import Config.UnauthorizedException;
import Models.PasswordResetToken;
import Models.Permission;
import Models.RefreshToken;
import Models.StatutUtilisateur;
import Models.Administrateur;
import Models.Employe;
import Models.Entreprise;
import Models.Huilerie;
import Models.Utilisateur;
import Repositories.PasswordResetTokenRepository;
import Repositories.PermissionRepository;
import Repositories.RefreshTokenRepository;
import Repositories.UtilisateurRepository;
import dto.AuthPermissionDTO;
import dto.AuthResponseDTO;
import dto.AuthUtilisateurDTO;
import dto.ProfileUpdateRequestDTO;
import dto.SignupRequestDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    @Value("${security.verification-email.expiration-hours:24}")
    private long verificationEmailExpirationHours;

    @Value("${app.mail.from:no-reply@gestionhuilerie.local}")
    private String mailFrom;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public AuthResponseDTO signup(SignupRequestDTO request) {
        utilisateurRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email deja utilise");
                });

        // Creer un Employe au lieu d'un Utilisateur au signup
        Employe employe = new Employe();
        employe.setNom(request.getNom());
        employe.setPrenom(request.getPrenom());
        employe.setEmail(request.getEmail());
        employe.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        employe.setTelephone(request.getTelephone());
        employe.setProfil(null);
        employe.setActif(StatutUtilisateur.ACTIF);
        employe.setHuilerieEmp(null); // Huilerie assignee par admin ultérieurement

        // Verification email obligatoire avant login
        employe.setEmailVerified(false);
        employe.setVerificationToken(UUID.randomUUID().toString());
        employe.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));

        Utilisateur saved = utilisateurRepository.save(employe);
        if (saved instanceof Employe savedEmploye) {
            savedEmploye.setIdEmploye(savedEmploye.getIdUtilisateur());
            saved = utilisateurRepository.save(savedEmploye);
        }
        sendVerificationEmail(saved);

        // Pas de JWT au signup tant que l'email n'est pas verifie
        return buildAuthResponse(saved, null, null);
    }

    public AuthResponseDTO login(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe invalide"));

        if (!Boolean.TRUE.equals(utilisateur.getEmailVerified())) {
            throw new UnauthorizedException("Veuillez verifier votre email avant de vous connecter");
        }

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

    public AuthResponseDTO verifyEmail(String token) {
        Utilisateur utilisateur = utilisateurRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de verification invalide"));

        if (utilisateur.getVerificationTokenExpiresAt() == null ||
                utilisateur.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de verification expire. Ce lien est valable 24 heures.");
        }

        utilisateur.setEmailVerified(true);
        utilisateur.setVerificationToken(UUID.randomUUID().toString());
        utilisateur.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        String jwtToken = jwtService.generateToken(saved);
        RefreshToken refreshToken = createRefreshToken(saved);

        return buildAuthResponse(saved, jwtToken, refreshToken.getToken());
    }

    public void resendVerificationEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        if (Boolean.TRUE.equals(utilisateur.getEmailVerified())) {
            return;
        }

        utilisateur.setVerificationToken(UUID.randomUUID().toString());
        utilisateur.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));
        utilisateurRepository.save(utilisateur);

        sendVerificationEmail(utilisateur);
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

        sendResetPasswordEmail(utilisateur, resetToken.getToken());
    }

    public AuthResponseDTO confirmResetPassword(String tokenValue, String nouveauMotDePasse) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token de reinitialisation invalide"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de reinitialisation expire. Ce lien est valable 30 minutes.");
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUtilisateurAndRevokedFalse(savedUtilisateur);
        for (RefreshToken refreshToken : activeTokens) {
            refreshToken.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);

        String token = jwtService.generateToken(savedUtilisateur);
        RefreshToken refreshToken = createRefreshToken(savedUtilisateur);

        return buildAuthResponse(savedUtilisateur, token, refreshToken.getToken());
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO me(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        return buildAuthResponse(utilisateur, null, null);
    }

    public AuthResponseDTO updateProfile(String currentEmail, ProfileUpdateRequestDTO request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
        boolean passwordChanged = false;

        if (hasText(request.getEmail())) {
            utilisateurRepository.findByEmail(request.getEmail())
                    .filter(existing -> !existing.getIdUtilisateur().equals(utilisateur.getIdUtilisateur()))
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Email deja utilise");
                    });
            utilisateur.setEmail(request.getEmail().trim());
        }

        if (hasText(request.getNom())) {
            utilisateur.setNom(request.getNom().trim());
        }
        if (hasText(request.getPrenom())) {
            utilisateur.setPrenom(request.getPrenom().trim());
        }
        if (request.getTelephone() != null) {
            utilisateur.setTelephone(request.getTelephone().trim());
        }

        boolean wantsPasswordChange = hasText(request.getCurrentPassword())
                || hasText(request.getNewPassword())
                || hasText(request.getConfirmPassword());

        if (wantsPasswordChange) {
            if (!hasText(request.getCurrentPassword()) || !hasText(request.getNewPassword()) || !hasText(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Ancien mot de passe, nouveau mot de passe et confirmation sont obligatoires");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), utilisateur.getMotDePasse())) {
                throw new IllegalArgumentException("Mot de passe actuel incorrect");
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("La confirmation du nouveau mot de passe est invalide");
            }
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
            passwordChanged = true;
        }

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);

        if (!passwordChanged) {
            return buildAuthResponse(savedUtilisateur, null, null);
        }

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUtilisateurAndRevokedFalse(savedUtilisateur);
        for (RefreshToken refreshToken : activeTokens) {
            refreshToken.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);
        permissionService.evictUserPermissions(savedUtilisateur.getIdUtilisateur());

        String token = jwtService.generateToken(savedUtilisateur);
        RefreshToken refreshToken = createRefreshToken(savedUtilisateur);
        return buildAuthResponse(savedUtilisateur, token, refreshToken.getToken());
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
        if (utilisateur instanceof Administrateur admin) {
            Entreprise entreprise = admin.getEntrepriseAdmin();
            authUtilisateurDTO.setEntrepriseId(entreprise != null ? entreprise.getIdEntreprise() : null);
            authUtilisateurDTO.setHuilerieId(null);
        } else if (utilisateur instanceof Employe employe) {
            Huilerie huilerie = employe.getHuilerieEmp();
            authUtilisateurDTO.setHuilerieId(huilerie != null ? huilerie.getIdHuilerie() : null);
            Entreprise entreprise = huilerie != null ? huilerie.getEntreprise() : null;
            authUtilisateurDTO.setEntrepriseId(entreprise != null ? entreprise.getIdEntreprise() : null);
        } else {
            authUtilisateurDTO.setEntrepriseId(utilisateur.getEntreprise() != null ? utilisateur.getEntreprise().getIdEntreprise() : null);
            authUtilisateurDTO.setHuilerieId(utilisateur.getHuilerie() != null ? utilisateur.getHuilerie().getIdHuilerie() : null);
        }
        authUtilisateurDTO.setNom(utilisateur.getNom());
        authUtilisateurDTO.setPrenom(utilisateur.getPrenom());
        authUtilisateurDTO.setEmail(utilisateur.getEmail());
        authUtilisateurDTO.setProfil(utilisateur.getProfil() != null ? utilisateur.getProfil().getNom() : null);

        List<AuthPermissionDTO> permissions = utilisateur.getProfil() == null
                ? List.of()
                : permissionRepository.findByProfilIdWithModule(utilisateur.getProfil().getIdProfil())
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

    private void sendVerificationEmail(Utilisateur utilisateur) {
        javaMailSender.ifPresent(sender -> {
            try {
                String verificationLink = frontendBaseUrl + "/verify-email?token=" + utilisateur.getVerificationToken();
                String html = buildVerificationEmailHtml(utilisateur, verificationLink);

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(mailFrom);
                helper.setTo(utilisateur.getEmail());
                helper.setSubject("Verifiez votre adresse email - Gestion Huilerie");
                helper.setText(html, true);

                sender.send(message);
            } catch (Exception ignored) {
                // Le signup reste reussi meme si l'email ne part pas.
            }
        });
    }

    private void sendResetPasswordEmail(Utilisateur utilisateur, String resetToken) {
        javaMailSender.ifPresent(sender -> {
            try {
                String resetLink = frontendBaseUrl + "/reset-password/confirm?token=" + resetToken;
                String html = buildResetPasswordEmailHtml(utilisateur, resetLink);

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(mailFrom);
                helper.setTo(utilisateur.getEmail());
                helper.setSubject("Reinitialisation mot de passe - Huileria");
                helper.setText(html, true);

                sender.send(message);
            } catch (Exception ignored) {
                // La demande reste valide meme si l'email ne part pas.
            }
        });
    }

    private String buildVerificationEmailHtml(Utilisateur utilisateur, String verificationLink) {
        String prenom = utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "";
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verification d'email - Huileria</title>
                </head>
                <body style="margin:0;padding:20px;background:#f9f7f2;font-family:Segoe UI,Arial,sans-serif;color:#2c2c2c;">
                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                        <div style="background:linear-gradient(135deg,#4f6324 0%%,#6a8136 100%%);padding:36px 24px;text-align:center;border-bottom:4px solid #687f32;">
                            <h1 style="margin:0;color:#f4efe4;font-size:24px;font-weight:700;">Huileria</h1>
                            <p style="margin:8px 0 0;color:#efe3c9;font-size:13px;">Plateforme de gestion de production</p>
                        </div>

                        <div style="padding:34px 24px;">
                            <h2 style="margin:0 0 18px;color:#4f6324;font-size:22px;">Bonjour %s</h2>
                            <p style="margin:0 0 14px;line-height:1.7;">Merci de vous etre inscrit sur Gestion Huilerie.</p>
                            <p style="margin:0 0 20px;line-height:1.7;">Pour activer votre compte, veuillez cliquer sur le bouton ci-dessous :</p>

                            <div style="text-align:center;margin:28px 0;">
                                <a href="%s"
                                   style="display:inline-block;background:linear-gradient(135deg,#6a8136 0%%,#4f6324 100%%);color:#f4efe4;text-decoration:none;padding:13px 28px;border-radius:6px;font-weight:600;">
                                    Verifier mon email
                                </a>
                            </div>

                            <p style="margin:0;color:#7f7f7f;font-size:13px;text-align:center;">Ce lien expire dans 24 heures.</p>
                            <p style="margin:14px 0 0;color:#9a9a9a;font-size:12px;text-align:center;">Si vous n'avez pas cree de compte, ignorez cet email.</p>
                        </div>

                        <div style="border-top:1px solid #e8e3d8;background:#faf8f4;padding:18px 24px;text-align:center;">
                            <p style="margin:0;color:#9a9a9a;font-size:12px;">© 2026 Huileria - Plateforme de gestion de production</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(prenom, verificationLink);
    }

    private String buildResetPasswordEmailHtml(Utilisateur utilisateur, String resetLink) {
        String prenom = utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "";
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reinitialisation mot de passe - Huileria</title>
                </head>
                <body style="margin:0;padding:20px;background:#f9f7f2;font-family:Segoe UI,Arial,sans-serif;color:#2c2c2c;">
                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                        <div style="background:linear-gradient(135deg,#4f6324 0%%,#6a8136 100%%);padding:36px 24px;text-align:center;border-bottom:4px solid #687f32;">
                            <h1 style="margin:0;color:#f4efe4;font-size:24px;font-weight:700;">Huileria</h1>
                            <p style="margin:8px 0 0;color:#efe3c9;font-size:13px;">Systeme de production de huiles</p>
                        </div>

                        <div style="padding:34px 24px;">
                            <h2 style="margin:0 0 18px;color:#4f6324;font-size:22px;">Bonjour %s</h2>
                            <p style="margin:0 0 14px;line-height:1.7;">Nous avons recu une demande de reinitialisation de mot de passe pour votre compte.</p>
                            <p style="margin:0 0 20px;line-height:1.7;">Pour creer un nouveau mot de passe, cliquez sur le bouton ci-dessous :</p>

                            <div style="text-align:center;margin:28px 0;">
                                <a href="%s"
                                   style="display:inline-block;background:linear-gradient(135deg,#6a8136 0%%,#4f6324 100%%);color:#f4efe4;text-decoration:none;padding:13px 28px;border-radius:6px;font-weight:600;">
                                    Creer un nouveau mot de passe
                                </a>
                            </div>

                            <p style="margin:0;color:#7f7f7f;font-size:13px;text-align:center;">Ce lien expire dans 30 minutes.</p>
                            <p style="margin:14px 0 0;color:#9a9a9a;font-size:12px;text-align:center;">Si vous n'avez pas demande de reinitialisation, ignorez cet email.</p>
                        </div>

                        <div style="border-top:1px solid #e8e3d8;background:#faf8f4;padding:18px 24px;text-align:center;">
                            <p style="margin:0;color:#9a9a9a;font-size:12px;">© 2026 Huileria - Plateforme de gestion de production</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(prenom, resetLink);
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}


