package com.utn.magtea.camada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CamadaCreateDTO(
        @NotBlank(message = "El nombre de la camada es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres") String nombre
) {}
