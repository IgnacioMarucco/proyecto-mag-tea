package com.utn.magtea.profesional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfesionalControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deberia_listarProfesionales_cuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/profesionales")
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deberia_rechazarAcceso_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/profesionales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberia_rechazarAcceso_cuandoRolInsuficiente() throws Exception {
        mockMvc.perform(get("/api/v1/profesionales")
                        .with(user("test@test.com").roles("CUERPO_MEDICO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberia_crearProfesional_cuandoDatosValidos() throws Exception {
        var dto = new ProfesionalCreateDTO("Luis", "Pérez", "luis@test.com", "351-000-0001", "pass1234", Role.CUERPO_MEDICO);

        mockMvc.perform(post("/api/v1/profesionales")
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("luis@test.com"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void deberia_retornar400_cuandoDatosInvalidos() throws Exception {
        var dtoInvalido = new ProfesionalCreateDTO("", "", "no-es-email", "", "123", null);

        mockMvc.perform(post("/api/v1/profesionales")
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberia_retornar404_cuandoIdNoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/profesionales/9999")
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberia_darDeBaja_cuandoProfesionalExiste() throws Exception {
        var dto = new ProfesionalCreateDTO("María", "Gómez", "maria@test.com", "351-000-0002", "pass1234", Role.CUERPO_MEDICO);
        var response = mockMvc.perform(post("/api/v1/profesionales")
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(response.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/v1/profesionales/" + id)
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/profesionales/" + id)
                        .with(user("test@test.com").roles("INVESTIGADOR_PRINCIPAL")))
                .andExpect(status().isNotFound());
    }
}
