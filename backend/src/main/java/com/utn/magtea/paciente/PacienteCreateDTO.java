package com.utn.magtea.paciente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteCreateDTO(
        Long formularioInteresId,
        @NotBlank(message = "El apellido del tutor es obligatorio") String apellidoTutor,
        @NotBlank(message = "El nombre del tutor es obligatorio") String nombreTutor,
        @NotBlank(message = "El correo del tutor es obligatorio")
        @Email(message = "El correo no es válido") String correoTutor,
        String telefono,
        @NotBlank(message = "El apellido del niño/a es obligatorio") String apellidoNino,
        @NotBlank(message = "El nombre del niño/a es obligatorio") String nombreNino,
        LocalDate fechaNacimientoNino,
        @NotNull(message = "El sexo es obligatorio") Sexo sexo,
        @NotNull(message = "La fecha de la primera visita es obligatoria") LocalDateTime fechaPrimeraVisita,
        String notas,
        @NotNull(message = "El tipo de paciente es obligatorio") TipoPaciente tipoPaciente,
        // Criterios de inclusión
        boolean criterioTEADSMV,
        boolean criterioTGDDSMIV,
        boolean criterioEdad,
        // Criterios de exclusión
        boolean epilepsia,
        boolean paralisisCerebral,
        boolean infeccionesCongenitas,
        boolean lesionesEstructuralesSNC,
        boolean facomatosis,
        boolean patologiasNeurometabolicas,
        boolean lesionesOcupantesEspacioSNC,
        boolean patologiaPsiquiatrica,
        boolean otrosSindromesGeneticos,
        boolean pubertadPrecoz,
        boolean consentimientoFirmado
) {}
