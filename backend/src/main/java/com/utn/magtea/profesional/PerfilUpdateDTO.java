package com.utn.magtea.profesional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PerfilUpdateDTO(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
        @NotBlank(message = "El teléfono es obligatorio") String telefono
) {}
