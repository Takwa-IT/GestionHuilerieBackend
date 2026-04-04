package Services;

import Models.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWN1cmVLZXlGb3JKSFdUU2lnbmluZw==", 86_400_000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");

        String token = jwtService.generateToken(utilisateur);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, utilisateur));
    }

    @Test
    void shouldRejectInvalidToken() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");

        assertFalse(jwtService.isTokenValid("invalid.token.value", utilisateur));
    }
}