package com.utn.magtea.camada;

import java.time.LocalDate;

public record CamadaListDTO(
        Long id,
        String nombre,
        LocalDate fechaNacimiento
) {}
