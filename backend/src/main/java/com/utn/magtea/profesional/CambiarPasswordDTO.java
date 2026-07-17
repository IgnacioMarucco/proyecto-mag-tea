package com.utn.magtea.profesional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordDTO(
        @NotBlank(message = "Debe ingresar su contraseña actual") String passwordActual,
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres") String passwordNueva
) {}
