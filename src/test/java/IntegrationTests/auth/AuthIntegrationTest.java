package IntegrationTests.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.LoginRequestDTO;
import dto.SignupRequestDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = org.example.gestionhuilerieback.GestionHuilerieBackApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Transactional
class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

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

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
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
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
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
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
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

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Then: Nouveau token généré
        String response = result.getResponse().getContentAsString();
        AuthResponseDTO refreshResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        
        assertThat(refreshResponse.getToken()).isNotEmpty();
        assertThat(refreshResponse.getToken()).isNotEqualTo(signupResponse.getToken());
    }
}
