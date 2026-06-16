package com.utn.magtea.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.profesional.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProfesionalRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        var profesional = new Profesional();
        profesional.setNombre("Admin");
        profesional.setApellido("Test");
        profesional.setEmail("admin@test.com");
        profesional.setPassword(passwordEncoder.encode("pass1234"));
        profesional.setRole(Role.INVESTIGADOR_PRINCIPAL);
        repository.save(profesional);
    }

    @Test
    void deberia_retornarToken_cuandoCredencialesValidas() throws Exception {
        var request = new LoginRequest("admin@test.com", "pass1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("INVESTIGADOR_PRINCIPAL"));
    }

    @Test
    void deberia_retornar401_cuandoPasswordIncorrecta() throws Exception {
        var request = new LoginRequest("admin@test.com", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberia_retornar401_cuandoEmailNoExiste() throws Exception {
        var request = new LoginRequest("noexiste@test.com", "pass1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberia_retornar400_cuandoDatosInvalidos() throws Exception {
        var request = new LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
