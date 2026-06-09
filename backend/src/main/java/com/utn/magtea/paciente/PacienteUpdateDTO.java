package com.utn.magtea.paciente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PacienteUpdateDTO(
        @NotBlank(message = "El apellido del tutor es obligatorio") String apellidoTutor,
        @NotBlank(message = "El nombre del tutor es obligatorio") String nombreTutor,
        @NotBlank(message = "El correo del tutor es obligatorio")
        @Email(message = "El correo no es válido") String correoTutor,
        String telefono,
        @NotBlank(message = "El apellido del niño/a es obligatorio") String apellidoNino,
        @NotBlank(message = "El nombre del niño/a es obligatorio") String nombreNino,
        LocalDate fechaNacimientoNino,
        @NotNull(message = "El sexo es obligatorio") Sexo sexo,
        String notas
) {}
