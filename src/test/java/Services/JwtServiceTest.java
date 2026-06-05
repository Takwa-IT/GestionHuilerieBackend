// Teste : Services.JwtService — Génération et validation des tokens JWT
package Services;

import Models.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("testSecretKeyForJunitTestsLongEnoughForHS256Algorithm12345", 86400000);
    }

    @Test
    @DisplayName("generateToken retourne token non null")
    void generateToken_retourneTokenNonNull() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@huilerie.tn");

        String token = jwtService.generateToken(utilisateur);

        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("extractEmail retourne email correct")
    void extractEmail_retourneEmailCorrect() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@huilerie.tn");

        String token = jwtService.generateToken(utilisateur);
        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("test@huilerie.tn");
    }

    @Test
    @DisplayName("isTokenValid retourne true quand token valide")
    void isTokenValid_retourneTrue_quandTokenValide() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@huilerie.tn");

        String token = jwtService.generateToken(utilisateur);

        assertThat(jwtService.isTokenValid(token, utilisateur)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid retourne false quand mauvais email")
    void isTokenValid_retourneFalse_quandMauvaisEmail() {
        Utilisateur user1 = new Utilisateur();
        user1.setEmail("user1@huilerie.tn");

        Utilisateur user2 = new Utilisateur();
        user2.setEmail("user2@huilerie.tn");

        String token = jwtService.generateToken(user1);

        assertThat(jwtService.isTokenValid(token, user2)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid retourne false quand token expire")
    void isTokenValid_retourneFalse_quandTokenExpire() {
        JwtService expiredJwtService = new JwtService("testSecretKeyForJunitTestsLongEnoughForHS256Algorithm12345", -1000);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@huilerie.tn");

        String token = expiredJwtService.generateToken(utilisateur);

        assertThat(expiredJwtService.isTokenValid(token, utilisateur)).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired retourne false quand token frais")
    void isTokenExpired_retourneFalse_quandTokenFrais() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@huilerie.tn");

        String token = jwtService.generateToken(utilisateur);

        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }
}

