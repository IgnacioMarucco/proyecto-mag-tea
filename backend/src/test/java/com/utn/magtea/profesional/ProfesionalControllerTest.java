package com.utn.magtea.profesional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfesionalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProfesionalRepository repository;

    @Test
    @WithMockUser(roles = "INVESTIGADOR_PRINCIPAL")
    void deberia_listarProfesionales_cuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/profesionales"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deberia_rechazarAcceso_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/profesionales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUERPO_MEDICO")
    void deberia_rechazarAcceso_cuandoRolInsuficiente() throws Exception {
        mockMvc.perform(get("/api/profesionales"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INVESTIGADOR_PRINCIPAL")
    void deberia_crearProfesional_cuandoDatosValidos() throws Exception {
        var dto = new ProfesionalCreateDTO("Luis", "Pérez", "luis@test.com", "pass1234", Role.CUERPO_MEDICO);

        mockMvc.perform(post("/api/profesionales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("luis@test.com"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "INVESTIGADOR_PRINCIPAL")
    void deberia_retornar400_cuandoDatosInvalidos() throws Exception {
        var dtoInvalido = new ProfesionalCreateDTO("", "", "no-es-email", "123", null);

        mockMvc.perform(post("/api/profesionales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "INVESTIGADOR_PRINCIPAL")
    void deberia_retornar404_cuandoIdNoExiste() throws Exception {
        mockMvc.perform(get("/api/profesionales/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "INVESTIGADOR_PRINCIPAL")
    void deberia_darDeBaja_cuandoProfesionalExiste() throws Exception {
        var dto = new ProfesionalCreateDTO("María", "Gómez", "maria@test.com", "pass1234", Role.SECRETARIA);
        var response = mockMvc.perform(post("/api/profesionales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(response.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/profesionales/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/profesionales/" + id))
                .andExpect(status().isNotFound());
    }
}
