package com.utn.magtea.paciente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteUpdateDTO(
        @NotBlank(message = "El apellido del tutor es obligatorio")
        @Size(max = 100, message = "El apellido del tutor no puede superar los 100 caracteres") String apellidoTutor,
        @NotBlank(message = "El nombre del tutor es obligatorio")
        @Size(max = 100, message = "El nombre del tutor no puede superar los 100 caracteres") String nombreTutor,
        @NotBlank(message = "El correo del tutor es obligatorio")
        @Email(message = "El correo no es válido") String correoTutor,
        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres") String telefono,
        @NotBlank(message = "El apellido del niño/a es obligatorio")
        @Size(max = 100, message = "El apellido del niño/a no puede superar los 100 caracteres") String apellidoNino,
        @NotBlank(message = "El nombre del niño/a es obligatorio")
        @Size(max = 100, message = "El nombre del niño/a no puede superar los 100 caracteres") String nombreNino,
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada") LocalDate fechaNacimientoNino,
        @NotNull(message = "El sexo es obligatorio") Sexo sexo,
        @Size(max = 2000, message = "Las notas no pueden superar los 2000 caracteres") String notas,
        LocalDateTime fechaPrimeraVisita,
        LocalDateTime fechaTurnoExtraccion
) {}
