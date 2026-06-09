package com.utn.magtea.formulariointeres;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FormularioInteresResponseDTO(
        Long id,
        LocalDate fechaContacto,
        String apellidoTutor,
        String nombreTutor,
        String correoTutor,
        String telefono,
        String apellidoNino,
        String nombreNino,
        LocalDate fechaNacimientoNino,
        Integer edadActual,
        Integer edadMeses,
        ComoConocioProyecto comoConocioProyecto,
        String otroComoConocio,
        String diasDisponibles,
        EstadoFormulario estado,
        boolean activo,
        LocalDateTime createdAt
) {}
