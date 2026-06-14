package IntegrationTests.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.LoginRequestDTO;
import dto.SignupRequestDTO;
import IntegrationTests.config.AbstractIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("loginSuccess - Utilisateur se connecte avec identifiants valides")
    void loginSuccess() throws Exception {
        // Given: Créer un utilisateur via signup
        SignupRequestDTO signupRequest = new SignupRequestDTO();
        signupRequest.setEmail("test@example.com");
        signupRequest.setMotDePasse("password123");
        signupRequest.setNom("Test");
        signupRequest.setPrenom("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // When: Login avec identifiants valides
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setMotDePasse("password123");

        var request = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.utilisateur.email").value("test@example.com"))
                .andReturn();

        // Then: Vérifier que le token JWT est valide
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO authResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        
        assertThat(authResponse.getToken()).isNotEmpty();
        assertThat(authResponse.getRefreshToken()).isNotEmpty();
        assertThat(authResponse.getUtilisateur().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("loginFailure - Utilisateur se connecte avec mot de passe incorrect")
    void loginFailure() throws Exception {
        // Given: Créer un utilisateur via signup
        SignupRequestDTO signupRequest = new SignupRequestDTO();
        signupRequest.setEmail("test@example.com");
        signupRequest.setMotDePasse("password123");
        signupRequest.setNom("Test");
        signupRequest.setPrenom("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // When: Login avec mot de passe incorrect
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setMotDePasse("wrongpassword");

        // Then: Erreur d'authentification
        var request = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("signupSuccess - Création d'un nouvel utilisateur")
    void signupSuccess() throws Exception {
        // Given: Données d'inscription valides
        SignupRequestDTO signupRequest = new SignupRequestDTO();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setMotDePasse("password123");
        signupRequest.setNom("New");
        signupRequest.setPrenom("User");

        // When: Inscription
        var request = post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.utilisateur.email").value("newuser@example.com"))
                .andReturn();

        // Then: Vérifier la réponse
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO authResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        
        assertThat(authResponse.getToken()).isNotEmpty();
        assertThat(authResponse.getUtilisateur().getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("refreshTokenSuccess - Renouvellement du token JWT")
    void refreshTokenSuccess() throws Exception {
        // Given: Créer un utilisateur et obtenir un refresh token
        SignupRequestDTO signupRequest = new SignupRequestDTO();
        signupRequest.setEmail("test@example.com");
        signupRequest.setMotDePasse("password123");
        signupRequest.setNom("Test");
        signupRequest.setPrenom("User");

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDTO signupResponse = objectMapper.readValue(
            signupResult.getResponse().getContentAsString(), 
            AuthResponseDTO.class
        );

        // When: Refresh token
        String refreshRequest = String.format("{\"refreshToken\":\"%s\"}", signupResponse.getRefreshToken());

        var request = post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest);
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Then: Nouveau token généré
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO refreshResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        
        assertThat(refreshResponse.getToken()).isNotEmpty();
    }
}
