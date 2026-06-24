package com.utn.magtea.camada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CamadaCreateDTO(
        @NotBlank(message = "El nombre de la camada es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres") String nombre,
        @NotNull(message = "La fecha de nacimiento es obligatoria") LocalDate fechaNacimiento
) {}
